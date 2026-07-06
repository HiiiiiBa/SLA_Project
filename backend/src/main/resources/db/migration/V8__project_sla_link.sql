-- Lie chaque projet à un SLA principal (filtrage employé par projet, pas par client entier)
ALTER TABLE projects ADD COLUMN IF NOT EXISTS sla_id BIGINT REFERENCES slas(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_projects_sla_id ON projects (sla_id);
