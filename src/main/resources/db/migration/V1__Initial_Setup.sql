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

CREATE TABLE envelope_request (
                                  id          SERIAL PRIMARY KEY,
                                  user_id     VARCHAR(255),
                                  name        VARCHAR(255),
                                  provider    VARCHAR(255),
                                  status      VARCHAR(255),
                                  created     TIMESTAMP,
                                  external_id VARCHAR(255) UNIQUE,
                                  version     BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE signer (
                        id          SERIAL PRIMARY KEY,
                        external_id VARCHAR(255) UNIQUE,
                        name        VARCHAR(255),
                        email       VARCHAR(255),
                        envelope_id BIGINT NOT NULL,
                        created     TIMESTAMP,
                        CONSTRAINT fk_signer_envelope FOREIGN KEY (envelope_id) REFERENCES envelope_request(id)
);

CREATE TABLE document (
                          id          SERIAL PRIMARY KEY,
                          external_id VARCHAR(255) UNIQUE,
                          filename    VARCHAR(255),
                          envelope_id BIGINT NOT NULL,
                          created     TIMESTAMP,
                          CONSTRAINT fk_document_envelope FOREIGN KEY (envelope_id) REFERENCES envelope_request(id)
);

CREATE TABLE envelope_event (
                                id              SERIAL PRIMARY KEY,
                                envelope_id     BIGINT NOT NULL,
                                previous_status VARCHAR(255),
                                new_status      VARCHAR(255),
                                source          VARCHAR(255),
                                occurred_at     TIMESTAMP,
                                CONSTRAINT fk_event_envelope FOREIGN KEY (envelope_id) REFERENCES envelope_request(id)
);

CREATE TABLE requirement (
                             id          SERIAL PRIMARY KEY,
                             external_id VARCHAR(255) UNIQUE,
                             envelope_id BIGINT NOT NULL,
                             document_id BIGINT NOT NULL,
                             signer_id   BIGINT NOT NULL,
                             created     TIMESTAMP,
                             CONSTRAINT fk_requirement_envelope FOREIGN KEY (envelope_id) REFERENCES envelope_request(id),
                             CONSTRAINT fk_requirement_document FOREIGN KEY (document_id) REFERENCES document(id),
                             CONSTRAINT fk_requirement_signer   FOREIGN KEY (signer_id)   REFERENCES signer(id)
);

CREATE INDEX idx_envelope_user_id  ON envelope_request(user_id);
CREATE INDEX idx_envelope_status   ON envelope_request(status);
CREATE INDEX idx_envelope_provider ON envelope_request(provider);
CREATE INDEX idx_envelope_ext_id   ON envelope_request(external_id);
CREATE INDEX idx_event_envelope_id ON envelope_event(envelope_id);
CREATE INDEX idx_signer_envelope   ON signer(envelope_id);
CREATE INDEX idx_document_envelope ON document(envelope_id);