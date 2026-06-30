-- Initial schema for SLA Monitoring (idempotent for existing databases)

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'USER', 'CLIENT')),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(512) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL REFERENCES users (id),
    expiry_date TIMESTAMP(6) NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS clients (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    project_name VARCHAR(255),
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_clients_email ON clients (email);

CREATE TABLE IF NOT EXISTS slas (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    uptime_target       DOUBLE PRECISION NOT NULL,
    response_time_limit INTEGER      NOT NULL,
    error_rate_limit    DOUBLE PRECISION NOT NULL,
    client_id           BIGINT       NOT NULL REFERENCES clients (id),
    created_at          TIMESTAMP(6) NOT NULL,
    updated_at          TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_slas_client_id ON slas (client_id);
CREATE INDEX IF NOT EXISTS idx_slas_status ON slas (status);

CREATE TABLE IF NOT EXISTS services (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    status     VARCHAR(10)  NOT NULL,
    sla_id     BIGINT       NOT NULL REFERENCES slas (id),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_services_sla_id ON services (sla_id);
CREATE INDEX IF NOT EXISTS idx_services_status ON services (status);

CREATE TABLE IF NOT EXISTS monitoring_metrics (
    id            BIGSERIAL PRIMARY KEY,
    timestamp     TIMESTAMP(6)       NOT NULL,
    response_time DOUBLE PRECISION   NOT NULL,
    status        VARCHAR(10)        NOT NULL,
    error_rate    DOUBLE PRECISION   NOT NULL,
    service_id    BIGINT             NOT NULL REFERENCES services (id),
    sla_id        BIGINT             NOT NULL REFERENCES slas (id),
    created_at    TIMESTAMP(6)       NOT NULL,
    updated_at    TIMESTAMP(6)       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_monitoring_metrics_sla_id ON monitoring_metrics (sla_id);
CREATE INDEX IF NOT EXISTS idx_monitoring_metrics_service_id ON monitoring_metrics (service_id);
CREATE INDEX IF NOT EXISTS idx_monitoring_metrics_timestamp ON monitoring_metrics (timestamp);

CREATE TABLE IF NOT EXISTS incidents (
    id          BIGSERIAL PRIMARY KEY,
    start_time  TIMESTAMP(6) NOT NULL,
    end_time    TIMESTAMP(6),
    severity    VARCHAR(20)  NOT NULL,
    description TEXT         NOT NULL,
    sla_id      BIGINT       NOT NULL REFERENCES slas (id),
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_incidents_sla_id ON incidents (sla_id);
CREATE INDEX IF NOT EXISTS idx_incidents_severity ON incidents (severity);
CREATE INDEX IF NOT EXISTS idx_incidents_start_time ON incidents (start_time);

CREATE TABLE IF NOT EXISTS alerts (
    id         BIGSERIAL PRIMARY KEY,
    type       VARCHAR(10)  NOT NULL,
    message    TEXT         NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    sla_id     BIGINT       NOT NULL REFERENCES slas (id),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_alerts_sla_id ON alerts (sla_id);
CREATE INDEX IF NOT EXISTS idx_alerts_status ON alerts (status);
CREATE INDEX IF NOT EXISTS idx_alerts_type ON alerts (type);

CREATE TABLE IF NOT EXISTS reports (
    id           BIGSERIAL PRIMARY KEY,
    sla_result   DOUBLE PRECISION NOT NULL,
    period_start TIMESTAMP(6)     NOT NULL,
    period_end   TIMESTAMP(6)     NOT NULL,
    generated_at TIMESTAMP(6)     NOT NULL,
    format       VARCHAR(10)      NOT NULL,
    sla_id       BIGINT           NOT NULL REFERENCES slas (id),
    created_at   TIMESTAMP(6)     NOT NULL,
    updated_at   TIMESTAMP(6)     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reports_sla_id ON reports (sla_id);
CREATE INDEX IF NOT EXISTS idx_reports_period ON reports (period_start, period_end);
