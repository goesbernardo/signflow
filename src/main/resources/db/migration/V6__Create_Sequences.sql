-- ================================================================
-- SignFlow — Migration V6
-- Cria as sequences que o Hibernate 6 espera encontrar.
-- O Hibernate 6 usa GenerationType.SEQUENCE por padrão e procura
-- sequences nomeadas como {table}_seq para cada entidade.
-- ================================================================

CREATE SEQUENCE IF NOT EXISTS envelope_request_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS signer_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS document_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS envelope_event_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS requirement_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS users_seq
    START WITH 1 INCREMENT BY 50;