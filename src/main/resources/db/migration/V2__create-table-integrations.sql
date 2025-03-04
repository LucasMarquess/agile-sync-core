CREATE SEQUENCE IF NOT EXISTS integration_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS user_settings_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS trello_settings (
 id BIGINT NOT NULL DEFAULT nextval('integration_seq'),
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL,
 token VARCHAR(255) NOT NULL,
 board_id VARCHAR(255),
 CONSTRAINT pk_trello_settings PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_integrations_settings (
id BIGINT NOT NULL DEFAULT nextval('user_settings_seq'),
user_id BIGINT NOT NULL UNIQUE,
trello_settings_id BIGINT,
CONSTRAINT pk_user_integrations_settings PRIMARY KEY (id),
CONSTRAINT fk_user_integrations_user FOREIGN KEY (user_id)
    REFERENCES users (id),
CONSTRAINT fk_user_integrations_trello_settings FOREIGN KEY (trello_settings_id)
    REFERENCES trello_settings (id)
);
