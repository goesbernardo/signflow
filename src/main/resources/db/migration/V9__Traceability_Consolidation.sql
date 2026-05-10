-- V9: Consolidate traceability columns and new audit_log table

-- Adicionar email e last_login_at em users (updated_at, deleted_at, consent_at já estão na V8)
ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;

-- Adicionar updated_at em envelope_request
ALTER TABLE envelope_request ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Adicionar auth_method em signer
ALTER TABLE signer ADD COLUMN auth_method VARCHAR(50);

-- Adicionar http_status_code em outbound_webhook_delivery
ALTER TABLE outbound_webhook_delivery ADD COLUMN http_status_code INTEGER;

-- Adicionar metadata em envelope_event (usando TEXT para compatibilidade ampla, pode armazenar JSON)
ALTER TABLE envelope_event ADD COLUMN metadata TEXT;

-- Criar tabela dedicada para audit_log
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(255),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_resource ON audit_log(resource_type, resource_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
