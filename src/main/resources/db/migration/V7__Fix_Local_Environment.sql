-- ================================================================
-- SignFlow — Migration V7
-- Garante compatibilidade entre ambiente local e Render.
-- O banco local pode ter tabelas em maiúsculo (ENVELOPE_EVENT)
-- da migration original, enquanto V4 referencia em minúsculo.
-- Esta migration usa IF NOT EXISTS em tudo para ser segura
-- em qualquer ambiente.
-- ================================================================

-- Criar tabelas caso não existam (ambiente local limpo)
CREATE TABLE IF NOT EXISTS envelope_event (
                                              id              BIGSERIAL PRIMARY KEY,
                                              envelope_id     BIGINT NOT NULL,
                                              previous_status VARCHAR(255),
    new_status      VARCHAR(255),
    source          VARCHAR(255),
    occurred_at     TIMESTAMP,
    CONSTRAINT fk_event_envelope FOREIGN KEY (envelope_id)
    REFERENCES envelope_request(id)
    );

-- Índices com IF NOT EXISTS — seguros em qualquer ambiente
CREATE INDEX IF NOT EXISTS idx_event_envelope_id ON envelope_event(envelope_id);
CREATE INDEX IF NOT EXISTS idx_signer_envelope   ON signer(envelope_id);
CREATE INDEX IF NOT EXISTS idx_document_envelope ON document(envelope_id);

-- Sequences caso não existam (ambiente local sem V6)
CREATE SEQUENCE IF NOT EXISTS envelope_request_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS signer_seq           START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS document_seq         START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS envelope_event_seq   START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS requirement_seq      START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS users_seq            START WITH 1 INCREMENT BY 50;