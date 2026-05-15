-- Migration: V5__Add_Tenant_Support.sql
-- Descrição: Adiciona suporte a multi-tenancy com o campo tenant_id

-- 1. Adicionar tenant_id na tabela users
ALTER TABLE users ADD COLUMN tenant_id VARCHAR(50);
COMMENT ON COLUMN users.tenant_id IS 'ID do tenant ao qual o usuário pertence';

-- 2. Adicionar tenant_id na tabela envelope_request para isolamento de dados
ALTER TABLE envelope_request ADD COLUMN tenant_id VARCHAR(50);
COMMENT ON COLUMN envelope_request.tenant_id IS 'ID do tenant ao qual o envelope pertence';

-- 3. Criar índices para performance nas queries filtradas por tenant
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_envelope_tenant_id ON envelope_request(tenant_id);

-- 4. Atualizar registros existentes (opcional, dependendo do ambiente)
-- UPDATE users SET tenant_id = 'DEFAULT' WHERE tenant_id IS NULL;
-- UPDATE envelope_request SET tenant_id = 'DEFAULT' WHERE tenant_id IS NULL;
