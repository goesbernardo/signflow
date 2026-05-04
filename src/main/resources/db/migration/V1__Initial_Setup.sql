-- ================================================================
-- SignFlow — Migration V1: Schema inicial
-- Usuários são criados exclusivamente via PSQL Command no Render
-- ou via create-user.sh no servidor. Nunca via migration SQL.
-- ================================================================

CREATE TABLE users (
                       id         SERIAL PRIMARY KEY,
                       username   VARCHAR(255) UNIQUE NOT NULL,
                       password   VARCHAR(255)        NOT NULL,
                       role       VARCHAR(50)         NOT NULL DEFAULT 'USER',
                       active     BOOLEAN             NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE ENVELOPE_REQUEST (
                                  id          SERIAL PRIMARY KEY,
                                  user_id     VARCHAR(255),
                                  name        VARCHAR(255),
                                  provider    VARCHAR(255),
                                  status      VARCHAR(255),
                                  created     TIMESTAMP,
                                  external_id VARCHAR(255) UNIQUE,
                                  version     BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE SIGNER (
                        id          SERIAL PRIMARY KEY,
                        external_id VARCHAR(255) UNIQUE,
                        name        VARCHAR(255),
                        email       VARCHAR(255),
                        envelope_id BIGINT NOT NULL,
                        created     TIMESTAMP,
                        CONSTRAINT fk_signer_envelope FOREIGN KEY (envelope_id) REFERENCES ENVELOPE_REQUEST(id)
);

CREATE TABLE DOCUMENT (
                          id          SERIAL PRIMARY KEY,
                          external_id VARCHAR(255) UNIQUE,
                          filename    VARCHAR(255),
                          envelope_id BIGINT NOT NULL,
                          created     TIMESTAMP,
                          CONSTRAINT fk_document_envelope FOREIGN KEY (envelope_id) REFERENCES ENVELOPE_REQUEST(id)
);

CREATE TABLE ENVELOPE_EVENT (
                                id              SERIAL PRIMARY KEY,
                                envelope_id     BIGINT NOT NULL,
                                previous_status VARCHAR(255),
                                new_status      VARCHAR(255),
                                source          VARCHAR(255),
                                occurred_at     TIMESTAMP,
                                CONSTRAINT fk_event_envelope FOREIGN KEY (envelope_id) REFERENCES ENVELOPE_REQUEST(id)
);

CREATE TABLE REQUIREMENT (
                             id          SERIAL PRIMARY KEY,
                             external_id VARCHAR(255) UNIQUE,
                             envelope_id BIGINT NOT NULL,
                             document_id BIGINT NOT NULL,
                             signer_id   BIGINT NOT NULL,
                             created     TIMESTAMP,
                             CONSTRAINT fk_requirement_envelope FOREIGN KEY (envelope_id) REFERENCES ENVELOPE_REQUEST(id),
                             CONSTRAINT fk_requirement_document FOREIGN KEY (document_id) REFERENCES DOCUMENT(id),
                             CONSTRAINT fk_requirement_signer   FOREIGN KEY (signer_id)   REFERENCES SIGNER(id)
);

CREATE INDEX idx_envelope_user_id  ON ENVELOPE_REQUEST(user_id);
CREATE INDEX idx_envelope_status   ON ENVELOPE_REQUEST(status);
CREATE INDEX idx_envelope_provider ON ENVELOPE_REQUEST(provider);
CREATE INDEX idx_envelope_ext_id   ON ENVELOPE_REQUEST(external_id);
CREATE INDEX idx_event_envelope_id ON ENVELOPE_EVENT(envelope_id);
CREATE INDEX idx_signer_envelope   ON SIGNER(envelope_id);
CREATE INDEX idx_document_envelope ON DOCUMENT(envelope_id);