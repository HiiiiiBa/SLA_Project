ALTER TABLE alerts
    ADD COLUMN IF NOT EXISTS service_id BIGINT REFERENCES services (id);

CREATE INDEX IF NOT EXISTS idx_alerts_service_id ON alerts (service_id);
