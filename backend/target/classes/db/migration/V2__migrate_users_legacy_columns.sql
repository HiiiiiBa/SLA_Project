-- Migrate users table from legacy French column names to the new auth schema.
-- Idempotent: runs safely on fresh databases and legacy Hibernate-created schemas.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'nom'
    ) THEN
        ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(255);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(255);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE;

        UPDATE users
        SET first_name = prenom
        WHERE first_name IS NULL AND prenom IS NOT NULL;

        UPDATE users
        SET last_name = nom
        WHERE last_name IS NULL AND nom IS NOT NULL;

        UPDATE users
        SET password = mot_de_passe
        WHERE password IS NULL AND mot_de_passe IS NOT NULL;

        UPDATE users
        SET enabled = TRUE
        WHERE enabled IS NULL;

        ALTER TABLE users ALTER COLUMN first_name SET NOT NULL;
        ALTER TABLE users ALTER COLUMN last_name SET NOT NULL;
        ALTER TABLE users ALTER COLUMN password SET NOT NULL;
        ALTER TABLE users ALTER COLUMN enabled SET NOT NULL;

        ALTER TABLE users DROP COLUMN IF EXISTS nom;
        ALTER TABLE users DROP COLUMN IF EXISTS prenom;
        ALTER TABLE users DROP COLUMN IF EXISTS mot_de_passe;

        ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
        ALTER TABLE users ADD CONSTRAINT users_role_check
            CHECK (role IN ('ADMIN', 'USER', 'CLIENT'));

        UPDATE users SET role = 'USER' WHERE role IN ('MANAGER', 'VIEWER');
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'enabled'
    ) THEN
        ALTER TABLE users ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;

    ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
    ALTER TABLE users ADD CONSTRAINT users_role_check
        CHECK (role IN ('ADMIN', 'USER', 'CLIENT'));
END $$;
