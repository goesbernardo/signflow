CREATE TABLE REQUIREMENT (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE,
    envelope_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    signer_id BIGINT NOT NULL,
    created TIMESTAMP,
    CONSTRAINT fk_requirement_envelope FOREIGN KEY (envelope_id) REFERENCES ENVELOPE_REQUEST(id),
    CONSTRAINT fk_requirement_document FOREIGN KEY (document_id) REFERENCES DOCUMENT(id),
    CONSTRAINT fk_requirement_signer FOREIGN KEY (signer_id) REFERENCES SIGNER(id)
);
