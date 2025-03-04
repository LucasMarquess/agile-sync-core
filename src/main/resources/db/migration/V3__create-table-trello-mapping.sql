CREATE SEQUENCE IF NOT EXISTS trello_mapping_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS trello_mapping
(
    id BIGINT DEFAULT nextval('trello_mapping_seq') PRIMARY KEY,
    referent VARCHAR(255) NOT NULL,
    list_id VARCHAR(255) NOT NULL,
    list_name VARCHAR(255) NOT NULL,
    trello_settings_id BIGINT NOT NULL,

    CONSTRAINT fk_trello_settings
    FOREIGN KEY (trello_settings_id)
    REFERENCES trello_settings(id)
    ON DELETE CASCADE,

    CONSTRAINT uq_trello_settings_referent UNIQUE (trello_settings_id, referent)
    );
