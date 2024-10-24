CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    login    VARCHAR(40)  NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    email    VARCHAR(90)  NOT NULL UNIQUE,
    role     VARCHAR(50)  NOT NULL
);
