-- Migration: V6__Add_Refresh_Token.sql
-- Descrição: Cria as tabelas refresh_tokens e user_roles (ausente na migração V1)

-- 1. Criação da tabela refresh_tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 2. Criação da tabela user_roles (ElementCollection de UserEntity)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    PRIMARY KEY (user_id, role)
);

-- Índice para busca rápida por token
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
-- Índice para busca por usuário
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
-- Índice para busca de roles por usuário
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
