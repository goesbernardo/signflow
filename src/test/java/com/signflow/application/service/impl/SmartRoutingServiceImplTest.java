package com.signflow.application.service.impl;

import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.enums.ProviderSignature;
import com.signflow.infrastructure.persistence.entity.ProviderRoutingRuleEntity;
import com.signflow.infrastructure.persistence.repository.ProviderRoutingRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartRoutingServiceImplTest {

    @Mock
    private ProviderRoutingRuleRepository repository;

    @InjectMocks
    private SmartRoutingServiceImpl smartRoutingService;

    private String userId = "user-123";
    private CreateEnvelopeCommand cmd;

    @BeforeEach
    void setUp() {
        cmd = new CreateEnvelopeCommand("Test Envelope", null);
    }

    @Test
    void shouldRouteByCostThresholdWhenCostIsAbove() {
        // GIVEN: Custo estático simulado é 3.00. Threshold é 2.50. 
        // 3.00 > 2.50 -> Deve aplicar a regra.
        ProviderRoutingRuleEntity rule = new ProviderRoutingRuleEntity();
        rule.setId(1L);
        rule.setUserId(userId);
        rule.setPriority(1);
        rule.setConditionType("COST_THRESHOLD");
        rule.setConditionValue("2.50");
        rule.setProvider(ProviderSignature.DOCUSIGN);
        rule.setActive(true);

        when(repository.findAllByUserIdAndActiveTrueOrderByPriorityAsc(userId))
                .thenReturn(List.of(rule));

        // WHEN
        ProviderSignature result = smartRoutingService.route(userId, cmd);

        // THEN
        assertEquals(ProviderSignature.DOCUSIGN, result);
    }

    @Test
    void shouldNotRouteByCostThresholdWhenCostIsBelow() {
        // GIVEN: Custo estático simulado é 3.00. Threshold é 4.00. 
        // 3.00 < 4.00 -> Não deve aplicar a regra.
        ProviderRoutingRuleEntity rule = new ProviderRoutingRuleEntity();
        rule.setId(1L);
        rule.setUserId(userId);
        rule.setPriority(1);
        rule.setConditionType("COST_THRESHOLD");
        rule.setConditionValue("4.00");
        rule.setProvider(ProviderSignature.DOCUSIGN);
        rule.setActive(true);

        when(repository.findAllByUserIdAndActiveTrueOrderByPriorityAsc(userId))
                .thenReturn(List.of(rule));

        // WHEN
        ProviderSignature result = smartRoutingService.route(userId, cmd);

        // THEN
        // Deve retornar o padrão (CLICKSIGN) já que a regra não se aplica
        assertEquals(ProviderSignature.CLICKSIGN, result);
    }
}
