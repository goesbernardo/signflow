-- Adicionando suporte a COST_THRESHOLD no comentário da coluna condition_type
COMMENT ON COLUMN provider_routing_rule.condition_type IS 'ALWAYS, AUTH_METHOD, COST_THRESHOLD';
