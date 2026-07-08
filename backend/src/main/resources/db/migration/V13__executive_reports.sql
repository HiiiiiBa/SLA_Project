CREATE TABLE executive_reports (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT       NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    sla_id          BIGINT       NOT NULL REFERENCES slas (id) ON DELETE CASCADE,
    project_name    VARCHAR(255) NOT NULL,
    client_name     VARCHAR(255) NOT NULL,
    sla_name        VARCHAR(255) NOT NULL,
    period_start    TIMESTAMP(6) NOT NULL,
    period_end      TIMESTAMP(6) NOT NULL,
    generated_at    TIMESTAMP(6) NOT NULL,
    generated_by_id BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    kpi_summary     TEXT         NOT NULL,
    narrative       TEXT         NOT NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_executive_reports_project_id ON executive_reports (project_id);
CREATE INDEX idx_executive_reports_sla_id ON executive_reports (sla_id);
CREATE INDEX idx_executive_reports_generated_at ON executive_reports (generated_at DESC);
