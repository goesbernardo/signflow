-- ================================================================
-- SignFlow — Migration V3
-- Corrige tipos de coluna: SERIAL (INTEGER) → BIGSERIAL (BIGINT)
-- As entidades Java usam Long (@GeneratedValue) que mapeia para
-- BIGINT. O SERIAL criado na V1 é INTEGER — incompatível.
-- ================================================================

-- DOCUMENT
ALTER TABLE document ALTER COLUMN id TYPE BIGINT;
ALTER TABLE document ALTER COLUMN envelope_id TYPE BIGINT;

-- SIGNER
ALTER TABLE signer ALTER COLUMN id TYPE BIGINT;
ALTER TABLE signer ALTER COLUMN envelope_id TYPE BIGINT;

-- ENVELOPE_REQUEST
ALTER TABLE envelope_request ALTER COLUMN id TYPE BIGINT;

-- ENVELOPE_EVENT
ALTER TABLE envelope_event ALTER COLUMN id TYPE BIGINT;
ALTER TABLE envelope_event ALTER COLUMN envelope_id TYPE BIGINT;

-- REQUIREMENT
ALTER TABLE requirement ALTER COLUMN id TYPE BIGINT;
ALTER TABLE requirement ALTER COLUMN envelope_id TYPE BIGINT;
ALTER TABLE requirement ALTER COLUMN document_id TYPE BIGINT;
ALTER TABLE requirement ALTER COLUMN signer_id TYPE BIGINT;

-- USERS
ALTER TABLE users ALTER COLUMN id TYPE BIGINT;