-- Users table
CREATE TABLE users (
    id         BIGINT       NOT NULL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users (email);

-- URLs table
CREATE TABLE urls (
    id          BIGINT      NOT NULL PRIMARY KEY,
    short_code  VARCHAR(20) NOT NULL UNIQUE,
    long_url    TEXT        NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    expires_at  TIMESTAMP WITH TIME ZONE,
    click_count BIGINT      NOT NULL DEFAULT 0,
    user_id     BIGINT
);

CREATE INDEX idx_short_code  ON urls (short_code);
CREATE INDEX idx_expires_at  ON urls (expires_at);
CREATE INDEX idx_created_at  ON urls (created_at);

-- Link statistics table
CREATE TABLE link_stats (
    id           BIGSERIAL   NOT NULL PRIMARY KEY,
    short_code   VARCHAR(20) NOT NULL,
    click_date   DATE        NOT NULL,
    daily_clicks BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_short_code_date UNIQUE (short_code, click_date)
);

CREATE INDEX idx_stats_short_code ON link_stats (short_code);
CREATE INDEX idx_stats_date       ON link_stats (click_date);
