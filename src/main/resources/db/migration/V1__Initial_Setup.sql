-- ================================================================
-- SignFlow — Migration V1: Schema consolidado
-- Inclui estrutura de webhook (eventos, signatários, status)
-- ================================================================

-- ================================================================
-- USERS
-- ================================================================
CREATE TABLE users (
                       id         BIGSERIAL    PRIMARY KEY,
                       username   VARCHAR(255) UNIQUE NOT NULL,
                       password   VARCHAR(255)        NOT NULL,
                       role       VARCHAR(50)         NOT NULL DEFAULT 'USER',
                       active     BOOLEAN             NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ================================================================
-- ENVELOPE_REQUEST
-- ================================================================
CREATE TABLE envelope_request (
                                  id              BIGSERIAL    PRIMARY KEY,
                                  user_id         VARCHAR(255),
                                  name            VARCHAR(255),
                                  provider        VARCHAR(255),
                                  status          VARCHAR(50),
                                  provider_status VARCHAR(255),
                                  created         TIMESTAMP,
                                  external_id     VARCHAR(255) UNIQUE,
                                  version         BIGINT       NOT NULL DEFAULT 0
);

COMMENT ON COLUMN envelope_request.status          IS 'Status interno: PROCESSING | ACTIVE | CLOSED | CANCELED | EXPIRED | REFUSED | FAILED | DRAFT | PENDING';
COMMENT ON COLUMN envelope_request.provider_status IS 'Status exato do provedor: running | completed | canceled | draft';

-- ================================================================
-- SIGNER
-- ================================================================
CREATE TABLE signer (
                        id          BIGSERIAL    PRIMARY KEY,
                        external_id VARCHAR(255) UNIQUE,
                        name        VARCHAR(255),
                        email       VARCHAR(255),
                        status      VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
                        signed_at   TIMESTAMP,
                        ip_address  VARCHAR(100),
                        envelope_id BIGINT       NOT NULL,
                        created     TIMESTAMP,
                        CONSTRAINT fk_signer_envelope FOREIGN KEY (envelope_id) REFERENCES envelope_request(id)
);

COMMENT ON COLUMN signer.status     IS 'PENDING | SIGNED | REFUSED';
COMMENT ON COLUMN signer.signed_at  IS 'Momento exato da assinatura (preenchido via webhook sign)';
COMMENT ON COLUMN signer.ip_address IS 'IP do signatário no momento da assinatura';

-- ================================================================
-- DOCUMENT
-- ================================================================
CREATE TABLE document (
                          id          BIGSERIAL    PRIMARY KEY,
                          external_id VARCHAR(255) UNIQUE,
                          filename    VARCHAR(255),
                          envelope_id BIGINT       NOT NULL,
                          created     TIMESTAMP,
                          CONSTRAINT fk_document_envelope FOREIGN KEY (envelope_id) REFERENCES envelope_request(id)
);

-- ================================================================
-- REQUIREMENT
-- ================================================================
CREATE TABLE requirement (
                             id          BIGSERIAL    PRIMARY KEY,
                             external_id VARCHAR(255) UNIQUE,
                             envelope_id BIGINT       NOT NULL,
                             document_id BIGINT       NOT NULL,
                             signer_id   BIGINT       NOT NULL,
                             created     TIMESTAMP,
                             CONSTRAINT fk_requirement_envelope FOREIGN KEY (envelope_id) REFERENCES envelope_request(id),
                             CONSTRAINT fk_requirement_document FOREIGN KEY (document_id) REFERENCES document(id),
                             CONSTRAINT fk_requirement_signer   FOREIGN KEY (signer_id)   REFERENCES signer(id)
);

-- ================================================================
-- ENVELOPE_EVENT
-- Registra cada mudança de estado do envelope.
-- source         = "API" ou "WEBHOOK"
-- provider_event = evento exato da ClickSign (sign, cancel, etc.)
-- signer_id      = preenchido nos eventos de sign/refusal
-- ================================================================
CREATE TABLE envelope_event (
                                id              BIGSERIAL    PRIMARY KEY,
                                envelope_id     BIGINT       NOT NULL,
                                signer_id       BIGINT,
                                previous_status VARCHAR(50),
                                new_status      VARCHAR(50),
                                provider_status VARCHAR(255),
                                provider_event  VARCHAR(100),
                                source          VARCHAR(50),
                                occurred_at     TIMESTAMP,
                                CONSTRAINT fk_event_envelope FOREIGN KEY (envelope_id) REFERENCES envelope_request(id),
                                CONSTRAINT fk_event_signer   FOREIGN KEY (signer_id)   REFERENCES signer(id)
);

COMMENT ON COLUMN envelope_event.source          IS 'Origem da mudança: API | WEBHOOK';
COMMENT ON COLUMN envelope_event.provider_event  IS 'Evento exato do provedor: sign | cancel | close | auto_close | deadline | refusal | add_signer | remove_signer';
COMMENT ON COLUMN envelope_event.provider_status IS 'Status exato do provedor no momento do evento: running | completed | canceled | draft';
COMMENT ON COLUMN envelope_event.signer_id       IS 'Signatário que gerou o evento (sign, refusal, add_signer, remove_signer)';

-- ================================================================
-- SEQUENCES — Hibernate 6 usa SEQUENCE por padrão
-- ================================================================
CREATE SEQUENCE IF NOT EXISTS envelope_request_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS signer_seq           START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS document_seq         START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS envelope_event_seq   START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS requirement_seq      START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS users_seq            START WITH 1 INCREMENT BY 50;

-- ================================================================
-- ÍNDICES
-- ================================================================

-- envelope_request
CREATE INDEX idx_envelope_user_id   ON envelope_request(user_id);
CREATE INDEX idx_envelope_status    ON envelope_request(status);
CREATE INDEX idx_envelope_provider  ON envelope_request(provider);
CREATE INDEX idx_envelope_ext_id    ON envelope_request(external_id);

-- signer
CREATE INDEX idx_signer_envelope    ON signer(envelope_id);
CREATE INDEX idx_signer_external_id ON signer(external_id);
CREATE INDEX idx_signer_status      ON signer(status);

-- document
CREATE INDEX idx_document_envelope  ON document(envelope_id);

-- envelope_event
CREATE INDEX idx_event_envelope_id   ON envelope_event(envelope_id);
CREATE INDEX idx_event_provider_event ON envelope_event(provider_event);
CREATE INDEX idx_event_signer_id     ON envelope_event(signer_id);