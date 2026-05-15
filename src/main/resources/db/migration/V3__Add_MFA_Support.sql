-- Adicionar colunas de MFA na tabela users
ALTER TABLE users ADD COLUMN mfa_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN mfa_secret VARCHAR(255);

-- Criar tabela mfa_codes
CREATE TABLE mfa_codes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX idx_mfa_codes_user_code ON mfa_codes(user_id, code);
