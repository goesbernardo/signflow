package com.signflow.application.service.impl;

import com.signflow.application.service.SmartRoutingService;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.CreateFullEnvelopeCommand;
import com.signflow.domain.command.FullRequirementCommand;
import com.signflow.enums.ProviderSignature;
import com.signflow.infrastructure.persistence.entity.ProviderRoutingRuleEntity;
import com.signflow.infrastructure.persistence.repository.ProviderRoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementação do Smart Routing com suporte a regras dinâmicas por usuário.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmartRoutingServiceImpl implements SmartRoutingService {

    private final ProviderRoutingRuleRepository repository;

    @Override
    public ProviderSignature route(String userId, CreateEnvelopeCommand cmd) {
        log.info("Avaliando regras de Smart Routing para o usuário: {}", userId);
        List<ProviderRoutingRuleEntity> rules = repository.findAllByUserIdAndActiveTrueOrderByPriorityAsc(userId);

        for (ProviderRoutingRuleEntity rule : rules) {
            if (evaluate(rule, cmd)) {
                log.info("Regra {} aplicada. Provedor selecionado: {}", rule.getId(), rule.getProvider());
                return rule.getProvider();
            }
        }

        log.info("Nenhuma regra compatível encontrada. Usando provedor padrão: CLICKSIGN");
        return ProviderSignature.CLICKSIGN;
    }

    @Override
    public ProviderSignature route(String userId, CreateFullEnvelopeCommand cmd) {
        log.info("Avaliando regras de Smart Routing (Full) para o usuário: {}", userId);
        List<ProviderRoutingRuleEntity> rules = repository.findAllByUserIdAndActiveTrueOrderByPriorityAsc(userId);

        for (ProviderRoutingRuleEntity rule : rules) {
            if (evaluate(rule, cmd)) {
                log.info("Regra {} aplicada. Provedor selecionado: {}", rule.getId(), rule.getProvider());
                return rule.getProvider();
            }
        }

        log.info("Nenhuma regra compatível encontrada. Usando provedor padrão: CLICKSIGN");
        return ProviderSignature.CLICKSIGN;
    }

    private boolean evaluate(ProviderRoutingRuleEntity rule, Object cmd) {
        if ("ALWAYS".equals(rule.getConditionType())) {
            return true;
        }

        if ("AUTH_METHOD".equals(rule.getConditionType()) && rule.getConditionValue() != null) {
            if (cmd instanceof CreateFullEnvelopeCommand fullCmd) {
                // Se o comando completo tiver requisitos, verifica se algum deles usa o método de autenticação da regra
                if (fullCmd.requirements() != null) {
                    for (FullRequirementCommand req : fullCmd.requirements()) {
                        if (req.auth() != null && req.auth().name().equalsIgnoreCase(rule.getConditionValue())) {
                            return true;
                        }
                    }
                }
            }
            // Para CreateEnvelopeCommand simples, não temos o método de autenticação ainda, 
            // então a regra AUTH_METHOD não se aplica a menos que tenhamos mais contexto.
        }

        if ("COST_THRESHOLD".equals(rule.getConditionType()) && rule.getConditionValue() != null) {
            double threshold = Double.parseDouble(rule.getConditionValue());
            // Por ora, implementação estática conforme requisitos:
            // Clicksign é o provider atual padrão. Se o custo dele > threshold, ativa a regra.
            double currentProviderCost = 3.00; // Simulação de custo da Clicksign
            return currentProviderCost > threshold;
        }

        return false;
    }
}
