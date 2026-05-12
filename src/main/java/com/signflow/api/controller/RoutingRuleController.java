package com.signflow.api.controller;

import com.signflow.infrastructure.persistence.entity.ProviderRoutingRuleEntity;
import com.signflow.infrastructure.persistence.repository.ProviderRoutingRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/routing-rules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Smart Routing Rules", description = "Gerenciamento de regras de roteamento inteligente de provedores")
@SecurityRequirement(name = "Bearer Authentication")
public class RoutingRuleController {

    private final ProviderRoutingRuleRepository repository;

    @GetMapping
    @Operation(summary = "Listar regras", description = "Retorna todas as regras de roteamento do usuário autenticado.")
    public ResponseEntity<List<ProviderRoutingRuleEntity>> listRules() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(repository.findAllByUserIdOrderByPriorityAsc(userId));
    }

    @PostMapping
    @Operation(summary = "Criar regra", description = "Cria uma nova regra de roteamento.")
    public ResponseEntity<ProviderRoutingRuleEntity> createRule(@RequestBody @Valid ProviderRoutingRuleEntity rule) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        rule.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(rule));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar regra", description = "Atualiza uma regra existente.")
    public ResponseEntity<ProviderRoutingRuleEntity> updateRule(@PathVariable Long id, @RequestBody @Valid ProviderRoutingRuleEntity ruleUpdate) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findById(id)
                .filter(rule -> rule.getUserId().equals(userId))
                .map(rule -> {
                    rule.setPriority(ruleUpdate.getPriority());
                    rule.setConditionType(ruleUpdate.getConditionType());
                    rule.setConditionValue(ruleUpdate.getConditionValue());
                    rule.setProvider(ruleUpdate.getProvider());
                    rule.setActive(ruleUpdate.isActive());
                    return ResponseEntity.ok(repository.save(rule));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir regra", description = "Remove uma regra de roteamento.")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findById(id)
                .filter(rule -> rule.getUserId().equals(userId))
                .map(rule -> {
                    repository.delete(rule);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
