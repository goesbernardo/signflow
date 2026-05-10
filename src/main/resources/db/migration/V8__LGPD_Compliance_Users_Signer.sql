-- V8: LGPD compliance - add audit columns and soft delete to users table
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN consent_at TIMESTAMP;
ALTER TABLE users ADD COLUMN role VARCHAR(50) DEFAULT 'USER';

-- Add documentation/cpf to signer table
ALTER TABLE signer ADD COLUMN documentation VARCHAR(255);
