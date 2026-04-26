CREATE TABLE IF NOT EXISTS store_confirmation_token (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255),
    created_at  DATETIME,
    expires_at  DATETIME,
    confirmed_at DATETIME,
    user_id     BIGINT NOT NULL,
    CONSTRAINT fk_sct_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS store_fcm_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       VARCHAR(512) NOT NULL,
    active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,
    CONSTRAINT uk_store_fcm_token UNIQUE (token)
);

CREATE TABLE IF NOT EXISTS store_push_subscriptions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    endpoint        VARCHAR(2048) NOT NULL,
    p256dh          VARCHAR(512) NOT NULL,
    auth            VARCHAR(512) NOT NULL,
    expiration_time BIGINT,
    active          TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL
);
