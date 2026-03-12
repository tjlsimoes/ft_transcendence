CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    avatar      VARCHAR(255),
    elo         INTEGER      NOT NULL DEFAULT 1000,
    wins        INTEGER      NOT NULL DEFAULT 0,
    losses      INTEGER      NOT NULL DEFAULT 0,
    win_streak  INTEGER      NOT NULL DEFAULT 0,
    league      VARCHAR(50)  NOT NULL DEFAULT 'BRONZE',
    status      VARCHAR(50)  NOT NULL DEFAULT 'OFFLINE',
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);
