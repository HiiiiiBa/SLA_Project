-- Align alerts.status check constraint with AlertStatus enum (NEW, READ, RESOLVED).
-- Legacy Hibernate schema used notification delivery statuses (PENDING, SENT, FAILED).

ALTER TABLE alerts DROP CONSTRAINT IF EXISTS alerts_status_check;

ALTER TABLE alerts
    ADD CONSTRAINT alerts_status_check
        CHECK (status IN ('NEW', 'READ', 'RESOLVED'));
