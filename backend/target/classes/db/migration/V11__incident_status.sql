ALTER TABLE incidents
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

UPDATE incidents
SET status = CASE
    WHEN end_time IS NOT NULL THEN 'RESOLVED'
    WHEN assignee_id IS NOT NULL THEN 'IN_PROGRESS'
    ELSE 'OPEN'
END
WHERE status IS NULL;

ALTER TABLE incidents
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE incidents
    ADD CONSTRAINT chk_incidents_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED'));

CREATE INDEX IF NOT EXISTS idx_incidents_status ON incidents (status);
