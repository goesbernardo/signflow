-- Adiciona coluna para armazenar o status original do provedor
ALTER TABLE envelope_request ADD COLUMN provider_status VARCHAR(255);
ALTER TABLE envelope_event ADD COLUMN provider_status VARCHAR(255);

COMMENT ON COLUMN envelope_request.provider_status IS 'Status exato retornado pelo provedor (ex: ClickSign)';
COMMENT ON COLUMN envelope_event.provider_status IS 'Status exato retornado pelo provedor no momento do evento';
