CREATE TABLE IF NOT EXISTS approval_requests (
    id              BIGSERIAL PRIMARY KEY,
    requester_id    BIGINT       NOT NULL REFERENCES users (id),
    action_type     VARCHAR(50)  NOT NULL,
    target_type     VARCHAR(50)  NOT NULL,
    target_id       BIGINT       NOT NULL,
    target_label    VARCHAR(255) NOT NULL,
    reason          TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reviewer_id     BIGINT       REFERENCES users (id),
    review_comment  TEXT,
    reviewed_at     TIMESTAMP,
    executed_at     TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_approval_action_type CHECK (action_type IN (
        'DELETE_PROJECT', 'DELETE_TEAM', 'DELETE_SLA',
        'ARCHIVE_SLA', 'ACTIVATE_SLA', 'DEACTIVATE_SLA'
    )),
    CONSTRAINT chk_approval_target_type CHECK (target_type IN ('PROJECT', 'TEAM', 'SLA')),
    CONSTRAINT chk_approval_status CHECK (status IN (
        'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXECUTED', 'FAILED'
    ))
);

CREATE INDEX IF NOT EXISTS idx_approval_requests_status ON approval_requests (status);
CREATE INDEX IF NOT EXISTS idx_approval_requests_requester ON approval_requests (requester_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_approval_requests_pending_unique
    ON approval_requests (action_type, target_type, target_id)
    WHERE status = 'PENDING';
