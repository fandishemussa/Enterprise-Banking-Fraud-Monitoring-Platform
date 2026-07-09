# Quickstart

## Prerequisites

- Java 21
- Docker (for PostgreSQL)

## 1. Start PostgreSQL

```bash
cp .env.example .env
docker compose up -d
```

This starts PostgreSQL on `localhost:5544` with database `fraud_monitoring_db` (user/password `postgres`/`postgres`).

## 2. Run the backend

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8081` with the `dev` profile active by default, which seeds two sample
customers, two accounts, and two transactions (one low-risk, one that triggers a HIGH/CRITICAL alert) on first run.
It also seeds a **default admin user** on first run (see below).

Swagger UI: `http://localhost:8081/swagger-ui.html`

## 3. Log in

The API is secured by default (`SECURITY_ENABLED=true`). Every endpoint except `POST /api/v1/auth/login` requires a
`Authorization: Bearer <token>` header. A default admin is auto-created on first startup if no `ADMIN` user exists yet:

| Username | Password      | Role  |
|----------|---------------|-------|
| `admin`  | `Admin@12345` | ADMIN |

Override these via `DEFAULT_ADMIN_USERNAME` / `DEFAULT_ADMIN_EMAIL` / `DEFAULT_ADMIN_PASSWORD` / `DEFAULT_ADMIN_FULL_NAME`
env vars before first startup. To disable auth entirely for local testing, set `SECURITY_ENABLED=false` (not
recommended beyond local dev).

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "Admin@12345"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)
```

Every command below assumes `$TOKEN` is set and adds `-H "Authorization: Bearer $TOKEN"`.

Roles and what they can do: `ADMIN` (everything, including user management), `ANALYST` (create transactions/test
transactions, manage alerts and cases), `INVESTIGATOR` (manage alerts and cases, read-only elsewhere), `TESTER`
(submit test transactions only), `VIEWER` (read-only everywhere).

## 4. Exercise the golden-path flow

### Create a customer

```bash
curl -X POST http://localhost:8081/api/v1/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "fullName": "Jane Doe",
        "email": "jane.doe@example.com",
        "phone": "+1-555-0199",
        "country": "United States"
      }'
```

Response includes a generated `customerId`, e.g. `CUS-1003`.

### Open an account

```bash
curl -X POST http://localhost:8081/api/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "CUS-1003",
        "accountType": "CHECKING",
        "balance": 5000,
        "currency": "USD"
      }'
```

Response includes a generated `accountId`, e.g. `ACC-1003`.

### Create a transaction (triggers scoring + alerting)

A normal transaction:

```bash
curl -X POST http://localhost:8081/api/v1/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "sourceAccountId": "ACC-1003",
        "amount": 75.00,
        "currency": "USD",
        "transactionType": "PAYMENT",
        "channel": "WEB",
        "merchantCategory": "GROCERY",
        "country": "United States",
        "deviceId": "device-jane-01",
        "ipAddress": "203.0.113.55"
      }'
```

A suspicious transaction (large amount, new device, new country, high-risk merchant — will trigger an alert):

```bash
curl -X POST http://localhost:8081/api/v1/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "sourceAccountId": "ACC-1003",
        "amount": 15000,
        "currency": "USD",
        "transactionType": "TRANSFER",
        "channel": "MOBILE",
        "merchantCategory": "CRYPTO",
        "country": "Nigeria",
        "deviceId": "device-unknown-99",
        "ipAddress": "198.51.100.77"
      }'
```

Or submit the same scenario through the **Test Transactions** page (`/test-transactions` in the dashboard, ADMIN/ANALYST/TESTER
only) to see the transaction, risk score, and fraud alert result in one response without needing separate follow-up calls -
see `POST /api/v1/test-transactions` below.

### Inspect the results

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/transactions
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/risk-scores
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/alerts
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/customers/CUS-1003/risk-profile
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/audit-logs
```

### Open an investigation case from the alert

```bash
# Replace ALERT-XXXX with the alertId returned above
curl -X POST http://localhost:8081/api/v1/cases \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "alertId": "ALERT-1002",
        "assignedTo": "analyst.smith",
        "notes": "Reviewing large crypto transfer from a new device and country"
      }'
```

### Update the case decision

```bash
curl -X PATCH http://localhost:8081/api/v1/cases/CASE-1001/decision \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"decision": "CONFIRMED_FRAUD"}'
```

## 5. User management (ADMIN only)

```bash
# Create a new user
curl -X POST http://localhost:8081/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "username": "analyst1",
        "email": "analyst1@fraud.local",
        "fullName": "Fraud Analyst One",
        "password": "Analyst@12345",
        "role": "ANALYST"
      }'

# List users
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/users

# Disable a user (soft-disable only, never physically deleted)
curl -X DELETE http://localhost:8081/api/v1/users/USER-1002 -H "Authorization: Bearer $TOKEN"
```

## 6. Test transactions (ADMIN, ANALYST, TESTER)

Submits a transaction and returns the transaction, risk score, and fraud alert result in one call - the same
endpoint backing the `/test-transactions` dashboard page:

```bash
curl -X POST http://localhost:8081/api/v1/test-transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "sourceAccountId": "ACC-1003",
        "amount": 15000,
        "currency": "USD",
        "transactionType": "TRANSFER",
        "channel": "WEB",
        "merchantCategory": "GAMBLING",
        "country": "NG",
        "deviceId": "device-unknown-99"
      }'
```

## Stopping

```bash
docker compose down          # keep data
docker compose down -v       # wipe the Postgres volume too
```
