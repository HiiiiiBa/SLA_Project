CREATE TABLE IF NOT EXISTS maintenance_windows (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    reason      TEXT,
    sla_id      BIGINT       NOT NULL REFERENCES slas (id),
    service_id  BIGINT       REFERENCES services (id),
    start_time  TIMESTAMP    NOT NULL,
    end_time    TIMESTAMP    NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_maintenance_window_status CHECK (status IN (
        'SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED'
    )),
    CONSTRAINT chk_maintenance_window_times CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_maintenance_windows_sla_id ON maintenance_windows (sla_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_windows_service_id ON maintenance_windows (service_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_windows_status ON maintenance_windows (status);
CREATE INDEX IF NOT EXISTS idx_maintenance_windows_start_time ON maintenance_windows (start_time);
CREATE INDEX IF NOT EXISTS idx_maintenance_windows_end_time ON maintenance_windows (end_time);
