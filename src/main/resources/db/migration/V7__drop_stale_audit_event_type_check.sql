-- The original V1 CHECK constraint hardcoded a whitelist of AuditEventType values as of the MVP.
-- Every event type added since (fraud rules, case timeline/notes, SLA/escalation, streaming) is
-- missing from it, so logging any of those event types fails with a check constraint violation
-- in real Postgres (never caught by unit tests, which mock the audit log service). No other table
-- in this schema mirrors its Java enum with a DB CHECK constraint - AppUser.role, FraudAlert.status,
-- InvestigationCase.status, etc. all rely on @Enumerated(EnumType.STRING) alone. Drop it here too
-- instead of re-whitelisting, so future AuditEventType additions don't need a matching migration.
ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS audit_logs_event_type_check;
