# Data Engineering Pipeline (v1.1.0)

## Overview

CSV batch ingestion layered on top of the v1.0.0 OLTP schema, modeling how a bank would land
external transaction files, validate them, and audit data quality — without ever touching the
live `banking_transactions` table used by real-time risk scoring.

## Layers

```
CSV file (multipart upload)
        |
        v
raw_transactions        <- untouched strings, one row per CSV line, always kept for replay
        |
        v
staging_transactions     <- type-coerced (amount parsed to BigDecimal, createdAt parsed to
        |                    LocalDateTime); parse failures are recorded, not thrown
        v
  [business validation]  <- TransactionCsvValidator: required fields, amount > 0, valid
        |                   enum values, known customer/account, not a future date, not a
        |                   duplicate transactionId
   pass |    fail
        v      v
clean_transactions   rejected_transactions   (+ dead_letter_transactions for rows that
                                                 failed to even parse structurally)
        |
        v
[auto-triggered data quality run over clean_transactions]
```

Rows that fail to parse at all (wrong column count, thrown exception) never reach
`staging_transactions` — they go straight to `dead_letter_transactions`, distinguishing
"structurally broken input" from "well-formed but business-invalid" (`rejected_transactions`).

## Pipeline observability

Every ingestion run and every data quality run wraps a `PipelineRun` (`pipeline` package),
independent of ingestion/quality specifics, so future Kafka consumers (v1.3.0) can reuse the
same observability model. Each `PipelineRun` has one `PipelineTaskRun` per step
(`parse-and-stage`, `validate-and-load`, or one per quality check) and any thrown error is
captured as a `PipelineError` tied back to the run/task.

## Data quality checks

Run automatically after every successful ingestion (`recordsAccepted > 0`), or manually via
`POST /api/v1/data-quality/run`. Each check runs over the full `clean_transactions` table:

| Check | What it verifies |
|---|---|
| `NotNullCheck` | `customer_id` is not null/blank |
| `UniqueCheck` | no duplicate `transaction_id` values |
| `AmountRangeCheck` | `amount > 0` |
| `AccountReferenceCheck` | `source_account_id` references an existing account |
| `ValidStatusCheck` | `status` / `transaction_type` are non-null typed enum values |
| `FreshnessCheck` | no `created_at` in the future; most recent row isn't older than 7 days |
| `VolumeAnomalyCheck` | latest ingestion's accepted-row count isn't a >50% swing from the recent average |

Each check produces one `DataQualityResult` (pass/fail counts) and zero or more
`DataQualityIssueDetail` rows describing the specific failing records.

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/ingestion/upload` | Upload a CSV file (multipart `file` field) |
| GET | `/api/v1/ingestion/runs` | Last 20 ingestion runs |
| GET | `/api/v1/ingestion/rejected-records` | Last 100 rejected rows with reasons |
| GET | `/api/v1/dead-letter/transactions` | All dead-lettered rows |
| POST | `/api/v1/dead-letter/transactions/{id}/retry` | Re-attempt structural parsing |
| PATCH | `/api/v1/dead-letter/transactions/{id}/ignore` | Mark an entry as ignored |
| POST | `/api/v1/data-quality/run` | Manually trigger a quality audit over `clean_transactions` |
| GET | `/api/v1/data-quality/runs` | Last 20 quality runs |
| GET | `/api/v1/data-quality/results` | Last 100 per-check results |
| GET | `/api/v1/data-quality/issues` | Last 200 individual issues |
| GET | `/api/v1/pipelines/runs` | Last 20 pipeline runs (ingestion + quality) |
| GET | `/api/v1/pipelines/runs/{runId}` | One run with its task breakdown |
| GET | `/api/v1/pipelines/metrics` | Success rate, average duration, last success/failure |
| GET | `/api/v1/pipelines/errors` | Last 50 pipeline errors |

## CSV format

Header (required, exact column order):

```
transactionId,customerId,sourceAccountId,destinationAccountId,amount,currency,transactionType,channel,merchantCategory,country,deviceId,ipAddress,status,createdAt
```

`destinationAccountId` and `merchantCategory` may be blank. `createdAt` accepts
`yyyy-MM-dd'T'HH:mm:ss` or `yyyy-MM-dd HH:mm:ss`. See
[data/samples/sample_transactions.csv](../data/samples/sample_transactions.csv) for a file
that exercises every rejection/dead-letter path.
