# Enterprise Banking Fraud Monitoring Platform

An enterprise-grade banking transaction risk and fraud monitoring platform. It simulates how a bank
or FinTech ingests transactions, scores them for fraud in real time (rules + ML), routes alerts
through a full analyst investigation workflow, and gives compliance/ops teams the tooling — case
management, SLA enforcement, customer locking, fraud-ring detection, analytics, notifications — to
run that workflow at scale, with an immutable audit trail behind every state change.

> **Status: v2.1.0 — enterprise hardening + advanced fraud-ops feature set.** Auth/RBAC, streaming
> ingestion, ML model monitoring, case management, SLA/escalation, customer locking, fraud-ring
> detection, bulk operations, notifications, and analytics are all built and verified end-to-end.
> See [Feature highlights](#feature-highlights) below and [Roadmap](#roadmap) for what's next.

## Table of contents

- [Architecture](#architecture)
- [Feature highlights](#feature-highlights)
- [Tech stack](#tech-stack)
- [Rule-based + ML risk scoring](#rule-based--ml-risk-scoring)
- [Authentication & roles](#authentication--roles)
- [Getting started](#getting-started)
- [API reference](#api-reference)
- [Testing](#testing)
- [Project structure](#project-structure)
- [Documentation index](#documentation-index)
- [Roadmap](#roadmap)

## Architecture

```
                     Next.js Dashboard (frontend/, port 3000)
                                   │
                                   ▼
        Spring Boot Core Banking/Fraud API (port 8080, Java 21 / Spring Boot 4.1)
                                   │
             ┌─────────────────────┼─────────────────────┐
             ▼                     ▼                      ▼
        PostgreSQL 16      Redpanda (Kafka API)     FastAPI ML Service (port 8000)
        (Flyway-managed)   real-time ingestion             │
                                                             ▼
                                                  scikit-learn Isolation Forest
                                                  + drift detection + retraining
```

Every transaction is scored twice: Spring Boot's rule engine runs first (7 pluggable rules,
including cross-customer fraud-ring detection), then it calls the FastAPI ML service for an
Isolation Forest anomaly score, and combines them 70% rule / 30% ML into a single final score. If
the ML service is unreachable, a Resilience4j circuit breaker trips, Spring Boot logs a warning,
continues with the rule score alone (`scoringSource: RULE_ONLY`), and transaction creation still
succeeds — fraud scoring never blocks banking.

Transactions can also flow in over Kafka-compatible Redpanda (`streaming/`) instead of the synchronous
REST endpoint, going through the identical scoring pipeline via a shared service, with a dead-letter
topic for anything that fails to process.

### Backend modules

| Module | Responsibility |
|---|---|
| `customer` | Customer profiles, risk level, PII encryption at rest |
| `customer/riskprofile` | Aggregated "Customer Risk 360" view (transactions, alerts, cases, trends) |
| `customer/lock` | Fraud lock/unlock workflow with admin-approval for investigator-initiated locks |
| `customer/network` | Fraud-ring detection: customers linked via shared device ID / IP address |
| `account` | Bank accounts owned by customers |
| `transaction` | Transaction ingestion; orchestrates scoring, alerting, and audit on create |
| `risk` | Rule-based risk scoring engine (7 pluggable rules) + hybrid ML integration |
| `risk/rulesmanagement` | Admin-configurable fraud rule thresholds/scores with full version history |
| `alert` | Fraud alert generation, assignment, bulk operations, escalation |
| `alert/sla` | SLA policies, breach/near-breach tracking, escalation history |
| `caseinvestigation` | Investigation case lifecycle, tied 1:1 to a fraud alert |
| `caseinvestigation/timeline` | Case activity timeline, analyst notes, status history |
| `notification` | On-the-fly notification feed (new alerts, escalations, pending lock requests) |
| `analytics` | Platform-wide reporting aggregates (merchant category, country, time-of-day, case outcomes) |
| `streaming` | Kafka/Redpanda producer/consumer for real-time transaction ingestion + dead letter queue |
| `ingestion` | CSV batch ingestion: raw/staging/clean/rejected layers |
| `quality` | Data quality checks run over `clean_transactions` |
| `pipeline` | Generic pipeline run/task/error tracking shared by ingestion, quality, and streaming |
| `audit` | Immutable, append-only audit trail for every state-changing action |
| `user` | User accounts, roles, JWT auth (with token-version revocation), account lockout |
| `security` | JWT issuing/parsing, request authentication filter, method-level RBAC guard |
| `testtransaction` | Single-call test harness: submit a transaction, get back transaction + risk score + alert |
| `common` | API envelope, exception handling, security/CORS config, ID sequences, PII crypto, idempotency |

## Feature highlights

**Fraud rules management** — the 6 built-in scoring rules (amount, device, country, frequency,
merchant, hour) plus a 7th cross-customer fraud-ring rule are all database-configurable: an admin
edits thresholds/scores from the Risk Rules page, every change is versioned with a full diff history,
and admins can also add brand-new amount-threshold rules that take effect immediately.

**Case investigation workflow** — every alert that moves to "Investigating" automatically gets an
investigation case (no manual step), pre-assigned to whoever the alert is assigned to. Cases carry a
full activity timeline, analyst notes, status history, and a decision panel (false positive / confirmed
fraud / needs more review). Reassigning or escalating the alert keeps the case's assignee in sync.

**Assignment-based visibility** — `ANALYST`/`INVESTIGATOR`/`TESTER` only see the alerts and cases
assigned to them (their working queue); `ADMIN` and `VIEWER` always see everything, for oversight.

**Customer Risk 360** — a single page aggregating a customer's full transaction/alert/case history,
risk and volume trend charts, device/country behavior, and now their fraud-ring linkages.

**Customer fraud lock** — `ADMIN` locks a customer immediately, blocking new transactions; `INVESTIGATOR`
can only submit a lock *request*, which stays pending until an admin approves or rejects it. A full
approval-queue page lets admins review and act on pending requests.

**Fraud-ring / network detection** — a dedicated rule flags a transaction the moment its device ID or
IP address is shared with a *different* customer, and the Customer Risk 360 page surfaces exactly who
else is linked and how.

**SLA monitoring & escalation** — configurable per-priority SLA policies, automatic breach/near-breach
tracking, and a full escalation trail (who escalated to whom, and why).

**Bulk operations** — multi-select rows in the Alerts/Cases tables to bulk-assign, bulk-escalate, or
bulk-update status in one action, with partial-success reporting (one bad ID doesn't abort the batch).

**Real-time notifications** — an in-app notification center polls for new alerts, escalations directed
at the current user, and (for admins) pending lock requests — scoped by the same visibility rules as
the rest of the platform.

**Reporting & analytics** — CSV export on every major table, plus a dedicated Analytics page (admin/
viewer) with fraud-trend charts: alerts by merchant category, by country, by hour of day, and case
resolution/decision breakdowns.

**Model monitoring & drift detection** — the ML service logs every prediction, tracks fallback rate and
score distribution, and detects feature drift by comparing live traffic against the training baseline
— all visible on the Model Monitoring dashboard, alongside retraining history.

**Streaming ingestion** — transactions can flow in over Kafka-compatible Redpanda instead of REST, through
the same scoring pipeline, with per-event status tracking and a dead-letter topic for failures.

**Security hardening** — JWT auth with per-user token-version revocation (instant logout-everywhere),
account lockout after repeated failed logins, PII field-level encryption at rest, ML-service API-key
auth, Resilience4j circuit breaker around the ML call, idempotency keys on transaction creation,
structured JSON logging with correlation IDs, Prometheus metrics, and a CI/CD pipeline with dependency
vulnerability scanning.

## Tech stack

| Layer | Stack |
|---|---|
| Backend | Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Kafka), Flyway, PostgreSQL 16 |
| ML service | Python, FastAPI, scikit-learn (Isolation Forest), pandas |
| Frontend | Next.js (App Router), TypeScript, Tailwind, TanStack Table, Recharts |
| Streaming | Redpanda (Kafka API-compatible) |
| Infra | Docker Compose, GitHub Actions CI/CD, Trivy dependency scanning |

## Rule-based + ML risk scoring

Every transaction is scored by independent rules (`risk/rules/`), each contributing points; the sum is
capped at 100 and mapped to a risk level:

| Score | Level |
|---|---|
| 0–30 | LOW |
| 31–60 | MEDIUM |
| 61–80 | HIGH |
| 81–100 | CRITICAL |

| Rule | Trigger | Points | Configurable? |
|---|---|---|---|
| `LargeAmountRule` | amount > threshold / > secondary threshold | +30 / +50 | Yes (Risk Rules page) |
| `NewDeviceRule` | first transaction from this device for the customer | +20 | Yes |
| `NewCountryRule` | first transaction from this country for the customer | +25 | Yes |
| `HighFrequencyTransactionRule` | > N transactions by the customer in the last 10 minutes | +35 | Yes |
| `HighRiskMerchantRule` | merchant category is CRYPTO / GAMBLING / HIGH_RISK_TRANSFER | +25 | Yes |
| `UnusualHourRule` | transaction between configurable overnight hours | +15 | Yes |
| `SharedDeviceOrIpRule` | device ID or IP address already used by a **different** customer (fraud ring signal) | +30 | Yes |
| `CustomAmountThresholdRule` | any admin-created custom amount-threshold rule | admin-defined | Admin can add new rules |

A `RiskScore` row records the rule score, the ML score (nullable if the ML service was unreachable),
the combined final score, `scoringSource` (`HYBRID` or `RULE_ONLY`), the triggered rules, and both a
rule-based and an ML-based plain-English explanation. Transactions scored HIGH or CRITICAL
automatically generate a `FraudAlert`, which — once assigned and moved to Investigating — automatically
gets a linked `InvestigationCase`.

`RiskScoringService` calls `POST {ML_SERVICE_URL}/api/v1/score` behind a Resilience4j circuit breaker
with a 1s connect / 2s read timeout. On success, `finalScore = 0.7 * ruleScore + 0.3 * mlScore`. On any
failure (timeout, connection refused, circuit open), scoring falls back to `finalScore = ruleScore`
with `scoringSource = RULE_ONLY` — transaction creation never fails because of the ML service. Every
call is audited (`ML_SCORE_REQUESTED`, `ML_SCORE_COMPLETED`, or `ML_SCORE_FAILED`).

## Authentication & roles

The API is secured by default (`SECURITY_ENABLED=true`, override to `false` for local testing without
a login flow). Every endpoint except `POST /api/v1/auth/login` requires `Authorization: Bearer <token>`.
A default `ADMIN` user is auto-created on first startup (`admin` / `Admin@12345` by default — override
via `DEFAULT_ADMIN_*` env vars); see [QUICKSTART.md](QUICKSTART.md) for the login walkthrough.

| Role | Can do |
|---|---|
| `ADMIN` | Everything — user management, fraud rule authoring, SLA policy management, customer lock/unlock, lock-request approval. Sees every alert/case. |
| `ANALYST` | Create transactions/test transactions, manage alerts and cases assigned to them |
| `INVESTIGATOR` | Manage alerts and cases assigned to them; can request (not directly apply) a customer lock, pending admin approval |
| `TESTER` | Submit test transactions; only sees alerts/cases assigned to them |
| `VIEWER` | Read-only everywhere — sees every alert/case and the Analytics dashboard, can't change anything |

**Assignment-based visibility**: `GET /api/v1/alerts` and `GET /api/v1/cases` are scoped per role
(`FraudAlertService#getAlertsVisibleTo`, `InvestigationCaseService#getCasesVisibleTo`) — `ANALYST`,
`INVESTIGATOR`, and `TESTER` only see alerts/cases where `assignedTo` matches their own username;
`ADMIN` and `VIEWER` always see everything. Reassigning or escalating an alert keeps its linked case's
assignee in sync automatically. Escalating or assigning works between any of `ADMIN`/`ANALYST`/
`INVESTIGATOR` (see the assignee dropdown on the Fraud Alerts page, backed by
`GET /api/v1/directory/assignable-users`).

Unauthenticated requests get `401`; authenticated requests with an insufficient role get `403`.

### Demo users (dev profile only)

Besides the default admin, `DemoUserSeeder` creates one account per non-admin role on startup (skipped
in `prod`, and per-username idempotent so it never touches an already-existing account):

| Username | Role | Password |
|---|---|---|
| `analyst1` / `analyst2` | `ANALYST` | `Demo@12345` |
| `investigator1` | `INVESTIGATOR` | `Demo@12345` |
| `viewer1` | `VIEWER` | `Demo@12345` |
| `tester1` | `TESTER` | `Demo@12345` |

`DataSeeder` also assigns a couple of the seeded alerts to `analyst1`/`analyst2`/`investigator1` so the
assignment-based visibility rule above has real data to show immediately — log in as `analyst1` and the
Fraud Alerts / Investigation Cases pages show just their queue, while `admin` or `viewer1` see everything.

## Getting started

```bash
docker compose up -d --build
```

This starts PostgreSQL, Redpanda, the Spring Boot API (8080), the FastAPI ML service (8000), and the
Next.js dashboard (3000). Flyway runs all migrations automatically on backend startup. See
[QUICKSTART.md](QUICKSTART.md) for the fastest path to a running instance, a full end-to-end curl
walkthrough, and how to run each service locally without Docker.

## API reference

Interactive Swagger UI is available at `http://localhost:8080/swagger-ui.html` once the app is running
(disabled in the `prod` profile).

<details>
<summary><strong>Customers, accounts, transactions, risk scores</strong></summary>

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/customers` | Create a customer |
| GET | `/api/v1/customers` | List customers |
| GET | `/api/v1/customers/{customerId}` | Get a customer |
| PUT | `/api/v1/customers/{customerId}` | Update a customer |
| GET | `/api/v1/customers/{customerId}/risk-profile` | Aggregated Customer Risk 360 view |
| GET | `/api/v1/customers/{customerId}/linked-customers` | Fraud-ring linkage: customers sharing a device/IP |
| GET | `/api/v1/customers/{customerId}/accounts` | List a customer's accounts |
| GET | `/api/v1/customers/{customerId}/transactions` | List a customer's transactions |
| GET | `/api/v1/customers/{customerId}/alerts` | List a customer's alerts |
| POST | `/api/v1/accounts` | Open an account for a customer |
| GET | `/api/v1/accounts` | List accounts |
| GET | `/api/v1/accounts/{accountId}` | Get an account |
| GET | `/api/v1/accounts/{accountId}/transactions` | List an account's transactions |
| POST | `/api/v1/transactions` | Create a transaction (triggers scoring + alerting + audit) |
| GET | `/api/v1/transactions` | List transactions |
| GET | `/api/v1/transactions/{transactionId}` | Get a transaction |
| GET | `/api/v1/risk-scores` | List all risk scores |
| GET | `/api/v1/transactions/{transactionId}/risk-score` | Get the risk score for a transaction |

</details>

<details>
<summary><strong>Customer fraud lock</strong></summary>

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/customers/{customerId}/lock` | Lock a customer (ADMIN: immediate; INVESTIGATOR: pending approval) |
| POST | `/api/v1/customers/{customerId}/unlock` | Unlock a customer (ADMIN only) |
| GET | `/api/v1/customers/{customerId}/lock-requests` | Lock request history for a customer |
| GET | `/api/v1/customers/lock-requests/pending` | Global pending lock-request queue |
| PATCH | `/api/v1/customers/lock-requests/{lockRequestId}/approve` | Approve a pending lock request (ADMIN only) |
| PATCH | `/api/v1/customers/lock-requests/{lockRequestId}/reject` | Reject a pending lock request (ADMIN only) |

</details>

<details>
<summary><strong>Fraud rules management</strong></summary>

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/fraud-rules` | List all fraud rules |
| GET | `/api/v1/fraud-rules/{ruleId}` | Get a fraud rule |
| POST | `/api/v1/fraud-rules` | Create a new custom fraud rule (ADMIN only) |
| PUT | `/api/v1/fraud-rules/{ruleId}` | Update a fraud rule's thresholds/score/severity (ADMIN only) |
| PATCH | `/api/v1/fraud-rules/{ruleId}/enable` | Enable a fraud rule (ADMIN only) |
| PATCH | `/api/v1/fraud-rules/{ruleId}/disable` | Disable a fraud rule (ADMIN only) |
| GET | `/api/v1/fraud-rules/{ruleId}/versions` | Full version history for a rule |

</details>

<details>
<summary><strong>Fraud alerts (including bulk operations)</strong></summary>

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/alerts` | List fraud alerts (assignment-scoped) |
| GET | `/api/v1/alerts/{alertId}` | Get a fraud alert |
| PATCH | `/api/v1/alerts/{alertId}/status` | Update alert status (auto-creates a case on → INVESTIGATING) |
| PATCH | `/api/v1/alerts/{alertId}/assign` | Assign an alert (keeps its linked case's assignee in sync) |
| POST | `/api/v1/alerts/{alertId}/escalate` | Escalate an alert to another user |
| PATCH | `/api/v1/alerts/bulk-status` | Bulk status update across multiple alerts |
| PATCH | `/api/v1/alerts/bulk-assign` | Bulk assign multiple alerts |
| POST | `/api/v1/alerts/bulk-escalate` | Bulk escalate multiple alerts |
| GET | `/api/v1/alerts/{alertId}/status-history` | Status change history |
| GET | `/api/v1/alerts/{alertId}/escalations` | Escalation history |
| GET | `/api/v1/directory/assignable-users` | Users eligible for assignment/escalation |

</details>

<details>
<summary><strong>Investigation cases</strong></summary>

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/cases` | Open an investigation case from an alert |
| GET | `/api/v1/cases` | List investigation cases (assignment-scoped) |
| GET | `/api/v1/cases/{caseId}` | Get an investigation case |
| PATCH | `/api/v1/cases/{caseId}` | Update case status/assignment/notes |
| PATCH | `/api/v1/cases/{caseId}/decision` | Record the analyst's decision |
| PATCH | `/api/v1/cases/bulk-update` | Bulk update status/assignment across multiple cases |
| GET | `/api/v1/cases/{caseId}/timeline` | Case activity timeline |
| GET/POST/PUT/DELETE | `/api/v1/cases/{caseId}/notes` | Analyst notes on a case |
| GET | `/api/v1/cases/{caseId}/status-history` | Case status change history |

</details>

<details>
<summary><strong>SLA monitoring & escalation</strong></summary>

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/sla/summary` | SLA compliance summary |
| GET | `/api/v1/sla/alerts` | All alerts with SLA status |
| GET | `/api/v1/sla/breached` | Breached alerts |
| GET | `/api/v1/sla/near-breach` | Alerts approaching breach |
| GET | `/api/v1/sla/policies` | List SLA policies |
| PUT | `/api/v1/sla/policies/{policyId}` | Update an SLA policy (ADMIN only) |

</details>

<details>
<summary><strong>Notifications & analytics</strong></summary>

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/notifications` | New alerts/escalations/pending-lock-requests for the current user |
| GET | `/api/v1/analytics/summary` | Platform-wide fraud trend aggregates (ADMIN/VIEWER only) |

</details>

<details>
<summary><strong>Streaming, data engineering & audit</strong></summary>

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/streaming/transactions/publish` | Publish a transaction onto the Kafka/Redpanda topic |
| GET | `/api/v1/streaming/events` | Recent streaming event log |
| GET | `/api/v1/streaming/metrics` | Streaming throughput/error metrics |
| GET/POST/PATCH | `/api/v1/streaming/dead-letter-events` | Dead-lettered streaming events (retry/ignore) |
| POST | `/api/v1/ingestion/upload` | Upload a CSV file for batch ingestion |
| GET | `/api/v1/ingestion/runs` | List recent ingestion runs |
| GET | `/api/v1/ingestion/rejected-records` | List recently rejected rows with reasons |
| GET/POST/PATCH | `/api/v1/dead-letter/transactions` | Dead-lettered (unparseable) CSV rows (retry/ignore) |
| POST | `/api/v1/data-quality/run` | Manually trigger a data quality audit |
| GET | `/api/v1/data-quality/runs`, `/results`, `/issues` | Data quality run history |
| GET | `/api/v1/pipelines/runs`, `/metrics`, `/errors` | Generic pipeline observability (ingestion + quality + streaming) |
| GET | `/api/v1/audit-logs` | Immutable audit trail |

</details>

<details>
<summary><strong>Auth & users</strong></summary>

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Log in, returns a JWT + user profile |
| POST | `/api/v1/auth/logout` | Revoke the current token (bumps token-version) |
| GET | `/api/v1/auth/me` | Get the current authenticated user |
| PATCH | `/api/v1/auth/change-password` | Change your own password |
| POST/GET/PUT/DELETE | `/api/v1/users` | Full user management (ADMIN only) |
| PATCH | `/api/v1/users/{userId}/status` | Activate/disable/lock a user (ADMIN only) |
| PATCH | `/api/v1/users/{userId}/reset-password` | Reset a user's password (ADMIN only) |
| POST | `/api/v1/test-transactions` | Submit a transaction and get back transaction + risk score + alert in one call |

</details>

<details>
<summary><strong>ML service (FastAPI, port 8000)</strong></summary>

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Health check |
| GET | `/api/v1/model-info` | Current model version, metadata, feature baselines |
| POST | `/api/v1/score` | Score a single transaction |
| POST | `/api/v1/batch-score` | Score a batch of transactions |
| POST | `/api/v1/retrain` | Trigger model retraining |
| GET | `/api/v1/monitoring/summary` | Prediction volume, fallback rate, risk distribution |
| GET | `/api/v1/monitoring/predictions` | Recent prediction log |
| GET | `/api/v1/monitoring/score-distribution` | Score histogram |
| GET | `/api/v1/monitoring/drift` | Per-feature drift vs. training baseline |
| GET | `/api/v1/monitoring/retraining-history` | Retraining run history |

</details>

## Testing

```bash
./mvnw test                          # backend (JUnit + Mockito, 40+ tests)
cd ml-service && pytest              # ML service
cd frontend && npm run build         # frontend type-check + build
```

CI runs all three on every push (see [Roadmap](#roadmap) / `.github/workflows/`), plus a dependency
vulnerability scan.

## Project structure

```
banking-transaction-risk-fraud-monitoring/
├── src/main/java/.../bankingtransactionriskfraudmonitoring/
│   ├── customer/            # Customer, risk profile, lock workflow, network/fraud-ring detection
│   ├── account/               # Account entity, DTOs, service, controller
│   ├── transaction/            # BankingTransaction; orchestrates risk + alert + audit
│   ├── risk/                   # RiskScore + rules/ (pluggable RiskRule beans) + rulesmanagement/
│   ├── alert/                  # FraudAlert + sla/ (SLA policies, escalation)
│   ├── caseinvestigation/      # InvestigationCase + timeline/ (notes, activity, status history)
│   ├── notification/           # On-the-fly notification feed
│   ├── analytics/              # Platform-wide reporting aggregates
│   ├── streaming/               # Kafka/Redpanda producer/consumer + dead letter queue
│   ├── ingestion/                # CSV batch ingestion pipeline
│   ├── quality/                  # Data quality checks
│   ├── pipeline/                  # Generic pipeline run/task/error tracking
│   ├── audit/                      # AuditLog entity, service, controller
│   ├── user/                        # Users, roles, JWT auth, account lockout
│   ├── security/                     # JWT filter, method-level RBAC guard
│   └── common/                        # API envelope, exceptions, security/CORS config, PII crypto
├── src/main/resources/db/migration/  # Flyway migrations (V1-V8+)
├── ml-service/                       # FastAPI + scikit-learn fraud scoring service
├── frontend/                         # Next.js dashboard
├── docker-compose.yml                 # Postgres, Redpanda, backend, ml-service, frontend
├── .env.example
├── QUICKSTART.md
└── docs/                              # Architecture, operations, data-engineering deep-dives
```

## Documentation index

- [QUICKSTART.md](QUICKSTART.md) — fastest path to a running instance + curl walkthrough
- [docs/operations.md](docs/operations.md) — secrets management, backup/DR, schema management, scaling
- [docs/data-engineering-pipeline.md](docs/data-engineering-pipeline.md) — ingestion/quality/pipeline design
- [frontend/README.md](frontend/README.md) — dashboard setup and structure
- [ml-service/README.md](ml-service/README.md) — ML service setup, training, and API details

## Roadmap

- **v1.1.0 (done)** — Data engineering foundation: CSV batch ingestion, data quality checks, dead letter queue, pipeline observability.
- **v1.2.0 (done)** — ML fraud scoring: FastAPI + Isolation Forest service, hybrid rule/ML score, Next.js dashboard.
- **v1.3.0 (done)** — Streaming: Kafka/Redpanda producer/consumer, real-time ingestion.
- **v2.0.0 (done)** — Enterprise hardening: JWT auth, RBAC, PII encryption, Flyway-managed schema, CI/CD, dependency scanning.
- **v2.1.0 (done)** — Advanced fraud ops: fraud rules management, case timeline/notes, Customer Risk 360, SLA/escalation, model monitoring/drift, customer locking, assignment-based visibility, bulk operations, notifications, analytics, fraud-ring detection.
- **Next** — Multi-factor authentication, webhook integrations for external case-management systems, SCD Type 2 customer risk history.
