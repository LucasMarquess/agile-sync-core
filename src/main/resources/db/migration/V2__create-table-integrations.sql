CREATE TABLE integrations
(
    id      SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE integrations_trello
(
    id     SERIAL PRIMARY KEY,
    user_id BIGINT       NOT NULL,
    token   VARCHAR(255) NOT NULL,
    key     VARCHAR(255) NOT NULL,
    UNIQUE (user_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);