# Operations Guide

Covers secrets management, backup/DR, API versioning, and scaling considerations for running this
platform beyond a local dev machine. None of this is wired up to an actual external service in this
repo (no Vault cluster, no cloud account, no CI runner) - it documents what production operation
requires and where the integration points are in the code.

## Schema management

`ddl-auto: update` (Hibernate guessing the schema from entities) is what this project shipped with,
and it caused real outages during development: adding a NOT NULL column to an already-populated
table, and a CHECK constraint on an enum column, both required manually dropping/altering tables by
hand because Hibernate's "update" mode only ever adds what's missing - it doesn't retroactively
widen a column, change a type, or fix a constraint on data that's already there.

Flyway is wired in (`spring-boot-starter-flyway` + `flyway-database-postgresql` in `pom.xml`,
`src/main/resources/db/migration/`) and is now **enabled by default** (`spring.flyway.enabled: true`,
overridable via `FLYWAY_ENABLED`), with `spring.jpa.hibernate.ddl-auto: validate` everywhere - Flyway
alone owns the schema; Hibernate only checks entities match what Flyway already created. Migrations so
far:
- `V1__baseline_schema.sql` - a straight `pg_dump --schema-only` of the schema as it stood before the
  initial hardening pass (21 tables).
- `V2__auth_hardening_and_idempotency.sql` - account lockout fields, JWT token versioning, widened PII
  columns, `idempotency_keys`.
- `V3__fraud_rules_management.sql` / `V4__case_timeline_and_notes.sql` /
  `V5__alert_sla_and_escalation.sql` / `V6__streaming_ingestion.sql` - one per feature, added
  alongside each new package.
- `V7__drop_stale_audit_event_type_check.sql` - see below.

**How this got fixed**: Flyway was left disabled for a while after V1/V2 (see git history), during
which `ddl-auto: update` silently took over and Hibernate auto-generated the tables for V3-V6 straight
from the JPA entities - so the live dev database had those tables without Flyway ever having run those
migration files, and `flyway_schema_history` only listed `V1`/`V2`. This is exactly the kind of drift
`ddl-auto` causes: it produced *tables* that matched the entities, but it has no way to know about a
plain SQL `CHECK` constraint like `V1`'s `audit_logs_event_type_check`, which hardcoded a whitelist of
`AuditEventType` values current only as of the MVP. Every event type added since (fraud rules, case
timeline/notes, SLA/escalation, streaming) was missing from it, so logging any of those failed with a
constraint violation in real Postgres - never caught by unit tests, which mock the audit log service.
`V7` drops that constraint outright rather than re-whitelisting (no other table in this schema mirrors
a Java enum with a DB CHECK constraint; `@Enumerated(EnumType.STRING)` alone is the pattern everywhere
else).

Re-enabling Flyway against that drifted dev database wasn't safe to do in place (Flyway would try to
re-run `CREATE TABLE fraud_rules` etc. and fail with "relation already exists"), so the dev Postgres
volume was dropped and rebuilt from scratch: `docker compose down -v` then `docker compose up -d`,
letting Flyway run `V1`-`V7` cleanly and the app's seeders (`DataSeeder`, `FraudRuleSeeder`,
`AlertSlaPolicySeeder`, `DefaultAdminSeeder`) repopulate baseline demo data on the empty schema.

Two real bugs turned up the first time Flyway was verified end-to-end, worth knowing if you ever
regenerate a baseline this way again:
- Spring Boot 4.x moved `FlywayAutoConfiguration` out of `spring-boot-autoconfigure` into a separate
  `spring-boot-starter-flyway` module. Depending on `flyway-core` alone silently does nothing -
  `spring.flyway.enabled` is read but no autoconfiguration class is even on the classpath to act on it,
  and nothing logs an error. If a Flyway-enabled app ever appears to boot fine but
  `flyway_schema_history` never gets created, check for this starter first.
- `pg_dump`'s output includes `\restrict`/`\unrestrict` meta-commands (a `psql`-only lock-safety
  feature, not real SQL). Flyway executes scripts via JDBC, not through `psql`, so these two lines
  broke migration with a raw `syntax error at or near "\"` - stripped them from `V1`.

All future schema changes must go through a new `V{n}__description.sql` file from here on - never
rely on `ddl-auto` again. If you ever need to point this app at an existing dev database that predates
this fix (still has `ddl-auto`-generated tables but no matching `flyway_schema_history` rows), the
options are the same as above: wipe and let Flyway rebuild it (simplest, safe for disposable local
data), or manually `INSERT INTO flyway_schema_history` rows for the missing versions before enabling.

## Secrets management

Every secret (`JWT_SECRET`, `DEFAULT_ADMIN_PASSWORD`, `DB_PASSWORD`, `ML_SERVICE_API_KEY`) currently
flows through plain environment variables (see `.env.example`, `docker-compose.yml`,
`application.yml`). That's fine for local dev; it is **not** sufficient for any environment reachable
outside your own machine.

- `SPRING_PROFILES_ACTIVE=prod` activates `application-prod.yml`, which enables
  `ProdSecretsValidator` (`common/config/ProdSecretsValidator.java`) - the app refuses to start if
  `JWT_SECRET` or `DEFAULT_ADMIN_PASSWORD` are missing, still the well-known local-dev defaults, or
  (for the JWT secret) too short to be a real HMAC key. This is a guardrail, not a secrets store.
- For real deployments, inject the same environment variables from a secrets manager instead of a
  plaintext `.env` file:
  - **HashiCorp Vault**: use the Vault Agent sidecar or `spring-cloud-vault-config` to populate env
    vars/`application.yml` properties at startup.
  - **AWS**: Secrets Manager + ECS/EKS secret injection, or Parameter Store (SecureString) with the
    task definition's `secrets` block.
  - **Kubernetes**: native `Secret` objects mounted as env vars, ideally sealed via
    `sealed-secrets` or `external-secrets` syncing from one of the above.
- Rotate `JWT_SECRET` by bumping every user's `tokenVersion` at the same time (or simply accept that
  all sessions invalidate on rotation, since token verification will fail once the key changes).
- Rotate `DB_PASSWORD`/`ML_SERVICE_API_KEY` via your secrets manager's rotation Lambda/job, then
  restart the affected services - neither is cached beyond process lifetime.

## Backup & disaster recovery

Nothing in this repo currently automates backups - this is the minimum a real deployment needs:

- **Postgres backups**: enable your managed Postgres provider's automated snapshots (e.g. RDS
  automated backups) with point-in-time recovery (PITR) enabled, at minimum daily snapshots + WAL
  archiving. If self-hosting Postgres, use `pg_basebackup`/WAL-G/pgBackRest on a schedule, storing
  backups in a separate region/account from the primary.
- **Retention**: audit_logs is append-only and compliance-relevant for a fraud platform - keep
  backups for at least as long as your regulatory retention requirement (commonly 5-7 years for
  financial audit trails).
- **ml-service model artifacts**: `ml-service/models/*.joblib` should be versioned/backed up
  independently of the DB (e.g. object storage with versioning) since retraining overwrites the file
  in place.
- **DR runbook outline**:
  1. Stand up a new Postgres instance from the latest snapshot/WAL replay in the DR region.
  2. Point `DB_HOST`/`DB_PORT` at the restored instance via the secrets manager, redeploy backend.
  3. Restore the last known-good ml-service model artifact (or accept fallback rule-only scoring
     until retrained - the platform is designed to degrade gracefully here, see
     `RiskScoringService`).
  4. Verify with the same smoke checks in [QUICKSTART.md](../QUICKSTART.md) (login, create
     transaction, confirm risk score + alert) before reopening traffic.
  5. Reconcile any transactions processed during the outage window via `audit_logs`/`pipeline_runs`.
- **RPO/RTO**: not yet defined for this project - decide based on real business requirements before
  going live; PITR gets RPO down to seconds, snapshot-only gets you to your snapshot interval.

## CORS

Both the Spring Boot backend (`cors.allowed-origins` / `CORS_ALLOWED_ORIGINS`,
`common/config/SecurityConfig.java`) and ml-service (`CORS_ALLOWED_ORIGINS`, `app/config.py`) read a
comma-separated origin allowlist from one env var each - they must be updated together whenever the
dashboard is deployed to a new origin. Neither sets `allowCredentials`/`allow_credentials` (auth is a
Bearer JWT in the `Authorization` header, not a cookie), so there's no wildcard-origin-plus-credentials
misconfiguration risk today - keep it that way if cookie-based auth is ever introduced, since
`Access-Control-Allow-Origin: *` combined with credentials is invalid per the CORS spec for a reason.

Per environment:
- **Local dev**: `http://localhost:3000` (the default).
- **Staging/prod**: set `CORS_ALLOWED_ORIGINS` to the exact dashboard origin(s) actually deployed
  (e.g. `https://fraud-dashboard.example.com`) - never reuse the `localhost:3000` default, and never
  set it to `*`.
- Multiple origins (e.g. staging + a preview environment) are supported via comma-separation:
  `CORS_ALLOWED_ORIGINS=https://fraud-dashboard.example.com,https://fraud-dashboard-preview.example.com`.

## API versioning

All endpoints are prefixed `/api/v1/`. There's no deprecation tooling yet - when a breaking change is
needed:
1. Introduce `/api/v2/...` alongside `/api/v1/...` rather than mutating v1's contract.
2. Document the deprecation timeline in this file and via a `Deprecation`/`Sunset` HTTP response
   header on the v1 endpoints being replaced.
3. Remove v1 only after confirming no consumer (check `audit_logs`/access logs) is still calling it.

## Scaling considerations

- **Stateless auth**: JWT + the `tokenVersion` check (`JwtAuthenticationFilter`) means any number of
  backend instances can serve requests behind a load balancer with no sticky sessions required -
  each does one extra DB read per authenticated request to check `tokenVersion`/status, which is the
  trade-off made for real logout/revocation (see task history in this repo's session notes).
- **Connection pooling**: Hikari's defaults are in effect (`spring.datasource.hikari.*` is unset).
  For real load, tune `maximum-pool-size` relative to `(core_count * 2) + effective_spindle_count`
  per Hikari's own sizing guidance, and make sure it's well under Postgres's `max_connections` once
  multiplied by instance count.
- **Read scaling**: list endpoints (`/transactions`, `/alerts`, `/audit-logs`, `/risk-scores`) are
  now paginated (see `Pageable` usage) - a read replica behind `@Transactional(readOnly = true)`
  routing is a natural next step if read load grows, since none of those queries need
  read-your-writes consistency beyond the current request.
- **ML service**: stateless FastAPI process; scale horizontally behind a load balancer. The
  Resilience4j circuit breaker on the Spring Boot side (`MlScoringClient`) means a slow/overloaded
  ML tier degrades to rule-only scoring rather than backing up every transaction request.
