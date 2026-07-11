# Enterprise Banking Fraud Monitoring Platform

An enterprise-style banking transaction risk and fraud monitoring platform. It simulates how banks and FinTech
companies ingest transactions, run rule-based risk scoring in real time, generate fraud alerts, and route them
through an analyst investigation workflow — with full audit logging at every step.

> **Status: v1.2.0 — backend + data engineering foundation + ML scoring + dashboard.** Kafka/Redpanda
> streaming is planned for v1.3.0 (see [Roadmap](#roadmap)).

## Architecture

```
Next.js Dashboard (frontend/, port 3000)
        |
        v
Spring Boot Core Banking/Fraud API (port 8080/8081)
        |
        |------ PostgreSQL
        |
        |------ Kafka/Redpanda            (v1.3.0, not yet built)
        |
        v
FastAPI ML Fraud Scoring Service (ml-service/, port 8000)
        |
        v
scikit-learn Isolation Forest model
```

Every transaction is scored twice: Spring Boot's rule engine runs first, then it calls the FastAPI
ML service for an Isolation Forest anomaly score, and combines them 70% rule / 30% ML into a single
final score. If the ML service is unreachable, Spring Boot logs a warning, continues with the rule
score alone (`scoringSource: RULE_ONLY`), and transaction creation still succeeds.

### v1.0.0 backend modules

| Module | Responsibility |
|---|---|
| `customer` | Customer profiles, risk level, risk-profile aggregation |
| `account` | Bank accounts owned by customers |
| `transaction` | Transaction ingestion; orchestrates scoring, alerting, and audit on create |
| `risk` | Rule-based risk scoring engine (6 pluggable rules) |
| `alert` | Fraud alert generation and analyst triage (status/assignment) |
| `caseinvestigation` | Investigation case lifecycle, tied to a fraud alert |
| `audit` | Immutable audit trail for every state-changing action |
| `common` | Shared API envelope, exception handling, security config, ID sequences |
| `ingestion` | CSV batch ingestion: raw/staging/clean/rejected layers, dead letter queue |
| `quality` | Data quality checks (7 checks) run over `clean_transactions` |
| `pipeline` | Generic pipeline run/task/error tracking shared by ingestion and data quality |
| `user` | User accounts, roles, JWT auth, default admin seeding |
| `security` | JWT issuing/parsing, request authentication filter |
| `testtransaction` | Single-call test harness that submits a transaction and returns transaction + risk score + fraud alert |

See [docs/data-engineering-pipeline.md](docs/data-engineering-pipeline.md) for the full ingestion/quality/pipeline
design.

## Tech stack

- Java 21, Spring Boot 4.1.0 (Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation)
- PostgreSQL 16
- Lombok, springdoc-openapi (Swagger UI)
- Maven, Docker Compose

## Domain model

All entities use human-readable public IDs generated from a DB-backed sequence (`common.IdSequenceService`), so
IDs stay unique across restarts: `CUS-1001`, `ACC-1001`, `TXN-1001`, `ALERT-1001`, `CASE-1001`.

```
customers ──< accounts ──< banking_transactions ──1:1── risk_scores
    │                              │                         │
    │                              └──1:1── fraud_alerts ─────┘
    │                                            │
    └────────────────────────────────< ─────────┘
                                            investigation_cases (1:1 with fraud_alerts)
                                            audit_logs (append-only, references any entity)
```

### Rule-based risk scoring

Every transaction is scored by six independent rules (`risk/rules/`), each contributing points; the sum is capped
at 100 and mapped to a risk level:

| Score | Level |
|---|---|
| 0–30 | LOW |
| 31–60 | MEDIUM |
| 61–80 | HIGH |
| 81–100 | CRITICAL |

| Rule | Trigger | Points |
|---|---|---|
| `LargeAmountRule` | amount > 5,000 / > 10,000 | +30 / +50 |
| `NewDeviceRule` | first transaction from this device for the customer | +20 |
| `NewCountryRule` | first transaction from this country for the customer | +25 |
| `HighFrequencyTransactionRule` | > 5 transactions by the customer in the last 10 minutes | +35 |
| `HighRiskMerchantRule` | merchant category is CRYPTO / GAMBLING / HIGH_RISK_TRANSFER | +25 |
| `UnusualHourRule` | transaction between 00:00–05:00 | +15 |

A `RiskScore` row records the rule score, the ML score (nullable if the ML service was unreachable),
the combined final score, `scoringSource` (`HYBRID` or `RULE_ONLY`), the triggered rules, and both a
rule-based and an ML-based plain-English explanation. Transactions scored HIGH or CRITICAL
automatically generate a `FraudAlert`.

### Hybrid ML scoring

`RiskScoringService` calls `POST {ML_SERVICE_URL}/api/v1/score` (see [ml-service/](ml-service/)) via
a `RestClient` with a 1s connect / 2s read timeout. On success, `finalScore = 0.7 * ruleScore + 0.3
* mlScore` and `scoringSource = HYBRID`. On any failure (timeout, connection refused, malformed
response), the exception is caught, logged as a warning, and scoring falls back to
`finalScore = ruleScore` with `scoringSource = RULE_ONLY` — transaction creation never fails because
of the ML service. Every call is audited (`ML_SCORE_REQUESTED`, `ML_SCORE_COMPLETED` or
`ML_SCORE_FAILED`).

## Authentication & roles

The API is secured by default (`SECURITY_ENABLED=true`, override to `false` for local testing without a login flow).
Every endpoint except `POST /api/v1/auth/login` requires `Authorization: Bearer <token>`. A default `ADMIN` user is
auto-created on first startup (`admin` / `Admin@12345` by default - override via `DEFAULT_ADMIN_*` env vars); see
[QUICKSTART.md](QUICKSTART.md) for the login walkthrough.

| Role | Can do |
|---|---|
| `ADMIN` | Everything, including user management. Sees every alert/case, not just their own. |
| `ANALYST` | Create transactions/test transactions, manage alerts and cases assigned to them |
| `INVESTIGATOR` | Manage alerts and cases assigned to them, read-only elsewhere |
| `TESTER` | Submit test transactions; only sees alerts/cases assigned to them |
| `VIEWER` | Read-only everywhere - sees every alert/case (for oversight), can't change anything |

**Assignment-based visibility**: `GET /api/v1/alerts` and `GET /api/v1/cases` are scoped per role
(`FraudAlertService#getAlertsVisibleTo`, `InvestigationCaseService#getCasesVisibleTo`) - `ANALYST`,
`INVESTIGATOR`, and `TESTER` only see alerts/cases where `assignedTo` matches their own username (their
working queue); `ADMIN` and `VIEWER` always see everything. Escalating or assigning an alert works
between any of `ADMIN`/`ANALYST`/`INVESTIGATOR` (see the assignee dropdown on the Fraud Alerts page,
backed by `GET /api/v1/directory/assignable-users`).

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
assignment-based visibility rule above has real data to show immediately - log in as `analyst1` and the
Fraud Alerts / Investigation Cases pages should show just their queue, while `admin` or `viewer1` see
everything.

## Getting started

See [QUICKSTART.md](QUICKSTART.md) for the fastest path to a running instance and a full end-to-end curl walkthrough.

## API reference

Interactive Swagger UI is available at `http://localhost:8081/swagger-ui.html` once the app is running (default
port; see [QUICKSTART.md](QUICKSTART.md) if 8080/5432 are already taken on your machine).

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/customers` | Create a customer |
| GET | `/api/v1/customers` | List customers |
| GET | `/api/v1/customers/{customerId}` | Get a customer |
| PUT | `/api/v1/customers/{customerId}` | Update a customer |
| GET | `/api/v1/customers/{customerId}/risk-profile` | Aggregated risk profile (transactions, open alerts, confirmed fraud cases) |
| POST | `/api/v1/accounts` | Open an account for a customer |
| GET | `/api/v1/accounts` | List accounts |
| GET | `/api/v1/accounts/{accountId}` | Get an account |
| GET | `/api/v1/customers/{customerId}/accounts` | List a customer's accounts |
| POST | `/api/v1/transactions` | Create a transaction (triggers scoring + alerting + audit) |
| GET | `/api/v1/transactions` | List transactions |
| GET | `/api/v1/transactions/{transactionId}` | Get a transaction |
| GET | `/api/v1/customers/{customerId}/transactions` | List a customer's transactions |
| GET | `/api/v1/accounts/{accountId}/transactions` | List an account's transactions |
| GET | `/api/v1/risk-scores` | List all risk scores |
| GET | `/api/v1/transactions/{transactionId}/risk-score` | Get the risk score for a transaction |
| GET | `/api/v1/alerts` | List fraud alerts |
| GET | `/api/v1/alerts/{alertId}` | Get a fraud alert |
| PATCH | `/api/v1/alerts/{alertId}/status` | Update alert status |
| PATCH | `/api/v1/alerts/{alertId}/assign` | Assign an alert to an analyst |
| GET | `/api/v1/customers/{customerId}/alerts` | List a customer's alerts |
| POST | `/api/v1/cases` | Open an investigation case from an alert |
| GET | `/api/v1/cases` | List investigation cases |
| GET | `/api/v1/cases/{caseId}` | Get an investigation case |
| PATCH | `/api/v1/cases/{caseId}` | Update case status/assignment/notes |
| PATCH | `/api/v1/cases/{caseId}/decision` | Record the analyst's decision |
| GET | `/api/v1/audit-logs` | List audit log entries |
| POST | `/api/v1/ingestion/upload` | Upload a CSV file for batch ingestion |
| GET | `/api/v1/ingestion/runs` | List recent ingestion runs |
| GET | `/api/v1/ingestion/rejected-records` | List recently rejected rows with reasons |
| GET | `/api/v1/dead-letter/transactions` | List dead-lettered (unparseable) rows |
| POST | `/api/v1/dead-letter/transactions/{id}/retry` | Re-attempt parsing a dead-lettered row |
| PATCH | `/api/v1/dead-letter/transactions/{id}/ignore` | Mark a dead-lettered row as ignored |
| POST | `/api/v1/data-quality/run` | Manually trigger a data quality audit |
| GET | `/api/v1/data-quality/runs` | List recent data quality runs |
| GET | `/api/v1/data-quality/results` | List recent per-check results |
| GET | `/api/v1/data-quality/issues` | List recent individual data quality issues |
| GET | `/api/v1/pipelines/runs` | List recent pipeline runs (ingestion + quality) |
| GET | `/api/v1/pipelines/runs/{runId}` | Get one pipeline run with its task breakdown |
| GET | `/api/v1/pipelines/metrics` | Success rate, average duration, last success/failure |
| GET | `/api/v1/pipelines/errors` | List recent pipeline errors |
| POST | `/api/v1/auth/login` | Log in, returns a JWT + user profile |
| GET | `/api/v1/auth/me` | Get the current authenticated user |
| PATCH | `/api/v1/auth/change-password` | Change your own password |
| POST | `/api/v1/users` | Create a user (ADMIN only) |
| GET | `/api/v1/users` | List users (ADMIN only) |
| GET | `/api/v1/users/{userId}` | Get a user (ADMIN only) |
| PUT | `/api/v1/users/{userId}` | Update a user (ADMIN only) |
| PATCH | `/api/v1/users/{userId}/status` | Activate/disable/lock a user (ADMIN only) |
| PATCH | `/api/v1/users/{userId}/reset-password` | Reset a user's password (ADMIN only) |
| DELETE | `/api/v1/users/{userId}` | Soft-disable a user (ADMIN only) |
| POST | `/api/v1/test-transactions` | Submit a transaction and get back transaction + risk score + fraud alert in one call (ADMIN/ANALYST/TESTER) |

## Roadmap

- **v1.1.0 (done)** — Data engineering foundation: CSV batch ingestion, raw/staging/clean layers, data quality checks, dead letter queue, pipeline observability.
- **v1.2.0 (done)** — ML fraud scoring: FastAPI + Isolation Forest service ([ml-service/](ml-service/)), hybrid rule/ML score, and the Next.js dashboard ([frontend/](frontend/)).
- **v1.3.0** — Streaming: Kafka/Redpanda producer/consumer, real-time scoring.
- **v2.0.0** — Enterprise hardening: JWT auth, RBAC, SCD Type 2 customer risk history, CI/CD.

## Project structure

```
enterprise-banking-fraud-monitoring-platform/
├── src/main/java/.../bankingtransactionriskfraudmonitoring/
│   ├── customer/            # Customer entity, DTOs, service, controller
│   ├── account/              # Account entity, DTOs, service, controller
│   ├── transaction/           # BankingTransaction; orchestrates risk + alert + audit
│   ├── risk/                  # RiskScore + rules/ (pluggable RiskRule beans)
│   ├── alert/                 # FraudAlert entity, DTOs, service, controller
│   ├── caseinvestigation/     # InvestigationCase entity, DTOs, service, controller
│   ├── audit/                 # AuditLog entity, service, controller
│   └── common/                # ApiResponse, ErrorResponse, GlobalExceptionHandler,
│                               # exceptions, IdSequenceService, SecurityConfig, OpenApiConfig, DataSeeder
├── docker-compose.yml         # PostgreSQL 16
├── .env.example
└── QUICKSTART.md
```
