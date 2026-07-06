-- Affectation manager ↔ clients (un manager peut gérer plusieurs clients)
CREATE TABLE IF NOT EXISTS client_managers (
    client_id   BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    manager_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (client_id, manager_id)
);

CREATE INDEX IF NOT EXISTS idx_client_managers_manager_id ON client_managers (manager_id);
