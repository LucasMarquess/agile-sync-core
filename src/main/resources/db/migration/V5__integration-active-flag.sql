ALTER TABLE trello_settings ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX IF NOT EXISTS idx_trello_settings_user_active
    ON trello_settings (user_id) WHERE active = TRUE;
