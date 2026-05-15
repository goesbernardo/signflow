ALTER TABLE users ADD COLUMN locked_until TIMESTAMP;

CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255),
    ip_address VARCHAR(45),
    success BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_attempts_username_created ON login_attempts(username, created_at);
CREATE INDEX idx_login_attempts_ip_created ON login_attempts(ip_address, created_at);
