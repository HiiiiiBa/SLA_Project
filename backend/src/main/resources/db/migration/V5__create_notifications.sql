CREATE TABLE IF NOT EXISTS notifications (
    id          BIGSERIAL PRIMARY KEY,
    alert_id    BIGINT       REFERENCES alerts (id) ON DELETE SET NULL,
    channel     VARCHAR(20)  NOT NULL CHECK (channel IN ('WEBSOCKET', 'EMAIL')),
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('SENT', 'FAILED')),
    recipient   VARCHAR(512),
    message     TEXT         NOT NULL,
    sla_id      BIGINT       NOT NULL,
    sla_name    VARCHAR(255) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_channel ON notifications (channel);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_sla_id ON notifications (sla_id);
