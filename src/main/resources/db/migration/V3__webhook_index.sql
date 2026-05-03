-- ================================================================
-- SignFlow — Migration V2
-- Índices para suportar o webhook handler em alta performance
-- ================================================================

-- O webhook busca envelopes por externalId em cada callback
-- Sem índice: full table scan a cada notificação da ClickSign
CREATE INDEX IF NOT EXISTS idx_envelope_external_id
    ON ENVELOPE_REQUEST(external_id);

-- Optimistic locking — garante consistência quando webhook
-- e API atualizam o mesmo envelope simultaneamente
ALTER TABLE ENVELOPE_REQUEST
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Registra qual usuário recebeu o documento assinado
-- (útil para notificações futuras)
ALTER TABLE ENVELOPE_EVENT
    ADD COLUMN IF NOT EXISTS triggered_by VARCHAR(255);

-- Comentário: WEBHOOK ou API — já existe na coluna source
-- triggered_by guarda o userId quando source = API,
-- ou 'clicksign-callback' quando source = WEBHOOK