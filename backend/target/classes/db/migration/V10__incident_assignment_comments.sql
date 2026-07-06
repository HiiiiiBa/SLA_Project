ALTER TABLE incidents
    ADD COLUMN IF NOT EXISTS assignee_id BIGINT REFERENCES users (id);

CREATE INDEX IF NOT EXISTS idx_incidents_assignee_id ON incidents (assignee_id);

CREATE TABLE IF NOT EXISTS incident_comments (
    id          BIGSERIAL PRIMARY KEY,
    incident_id BIGINT       NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    author_id   BIGINT       NOT NULL REFERENCES users (id),
    content     TEXT         NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_incident_comments_incident_id ON incident_comments (incident_id);
