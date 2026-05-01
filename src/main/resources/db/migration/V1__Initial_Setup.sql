CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE ENVELOPE_REQUEST (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255),
    provider VARCHAR(255),
    status VARCHAR(255),
    created TIMESTAMP,
    external_id VARCHAR(255) UNIQUE
);

CREATE TABLE SIGNER (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    email VARCHAR(255),
    envelope_id BIGINT NOT NULL,
    created TIMESTAMP,
    CONSTRAINT fk_signer_envelope FOREIGN KEY (envelope_id) REFERENCES ENVELOPE_REQUEST(id)
);

CREATE TABLE DOCUMENT (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE,
    filename VARCHAR(255),
    envelope_id BIGINT NOT NULL,
    created TIMESTAMP,
    CONSTRAINT fk_document_envelope FOREIGN KEY (envelope_id) REFERENCES ENVELOPE_REQUEST(id)
);

CREATE TABLE ENVELOPE_EVENT (
    id SERIAL PRIMARY KEY,
    envelope_id BIGINT NOT NULL,
    previous_status VARCHAR(255),
    new_status VARCHAR(255),
    source VARCHAR(255),
    occurred_at TIMESTAMP,
    CONSTRAINT fk_event_envelope FOREIGN KEY (envelope_id) REFERENCES ENVELOPE_REQUEST(id)
);

-- Inserir usuário admin padrão para produção com senha 'admin123' criptografada em BCrypt
-- Nota: Em um cenário real de "gerada aleatoriamente", isso seria injetado via variável de ambiente ou script de deploy.
-- Aqui usamos o valor fixo solicitado para manter compatibilidade com o login inicial se necessário, 
-- mas seguindo a regra de ser via migration.
INSERT INTO users (username, password) 
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.SZXkN.G'); 
