-- Adicionando coluna next_attempt_at para suporte a retry assíncrono
ALTER TABLE outbound_webhook_delivery ADD COLUMN next_attempt_at TIMESTAMP;

-- Atualizando status existentes para o novo padrão (se necessário, embora o enum seja String na JPA por enquanto)
-- Como a entidade usa String, não precisamos de cast aqui, mas vamos garantir que os nomes batam.
