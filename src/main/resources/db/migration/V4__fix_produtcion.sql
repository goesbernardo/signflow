-- ================================================================
-- SignFlow — Migration V3
-- Corrige dois problemas da V1:
-- 1. Remove o INSERT com placeholders que falha no Render
--    (Placeholder replacement causava erro na migração)
-- 2. Adiciona colunas que as entidades Java esperam encontrar
-- ================================================================

-- O INSERT da V1 falhou porque os placeholders
-- são do Flyway, não variáveis de ambiente.
-- Usuários são criados exclusivamente via create-user.sh no servidor.

-- Adicionar coluna 'role' que UserEntity referencia via getAuthorities()
-- (necessário para suporte futuro a ADMIN/USER)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'USER';

-- Adicionar coluna 'active' que isEnabled() vai precisar quando
-- UserEntity for atualizada com controle de acesso por usuário
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- Adicionar coluna 'created_at' para auditoria de criação de usuário
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();

-- Adicionar coluna 'version' no envelope para optimistic locking
-- (evita race condition entre webhook e API atualizando status simultâneo)
ALTER TABLE envelope_request
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Índices para performance nas queries mais comuns
-- (podem já existir na V1 de outros deploys — IF NOT EXISTS protege)
CREATE INDEX IF NOT EXISTS idx_envelope_user_id    ON envelope_request(user_id);
CREATE INDEX IF NOT EXISTS idx_envelope_status     ON envelope_request(status);
CREATE INDEX IF NOT EXISTS idx_envelope_provider   ON envelope_request(provider);
CREATE INDEX IF NOT EXISTS idx_envelope_ext_id     ON envelope_request(external_id);
CREATE INDEX IF NOT EXISTS idx_event_envelope_id   ON envelope_event(envelope_id);
CREATE INDEX IF NOT EXISTS idx_signer_envelope     ON signer(envelope_id);
CREATE INDEX IF NOT EXISTS idx_document_envelope   ON document(envelope_id);