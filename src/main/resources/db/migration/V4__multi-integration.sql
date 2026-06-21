ALTER TABLE trello_settings ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE trello_settings ADD COLUMN IF NOT EXISTS user_id BIGINT;

UPDATE trello_settings ts
SET user_id = uis.user_id
FROM user_integrations_settings uis
WHERE uis.trello_settings_id = ts.id;

UPDATE trello_settings SET name = 'Integração Trello' WHERE name IS NULL OR name = '';

DELETE FROM trello_settings WHERE user_id IS NULL;

ALTER TABLE trello_settings ALTER COLUMN name SET NOT NULL;
ALTER TABLE trello_settings ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE trello_settings
    ADD CONSTRAINT fk_trello_settings_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX IF NOT EXISTS idx_trello_settings_user_id ON trello_settings (user_id);

DROP TABLE IF EXISTS user_integrations_settings;
DROP SEQUENCE IF EXISTS user_settings_seq;
