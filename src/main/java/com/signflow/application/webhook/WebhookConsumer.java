package com.signflow.application.webhook;

import com.signflow.application.webhook.dto.WebhookReceivedEvent;
import com.signflow.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookConsumer {

    private final Map<String, WebhookHandler> handlers;

    @KafkaListener(topics = KafkaConfig.WEBHOOK_RECEIVED_TOPIC, groupId = "signflow-webhook-processors")
    public void consume(WebhookReceivedEvent event) {
        String provider = event.getProvider() != null ? event.getProvider().toLowerCase() : "";
        log.info("Consumindo webhook do Kafka: provedor={}, payload_length={}", provider, event.getPayload().length());

        WebhookHandler handler = handlers.get(provider);
        if (handler == null) {
            log.warn("Nenhum handler registrado para o provedor: {}. Handlers disponíveis: {}", provider, handlers.keySet());
            return;
        }

        try {
            handler.handle(provider, event.getPayload());
        } catch (Exception e) {
            log.error("Erro ao processar webhook do Kafka (provider={})", provider, e);
        }
    }
}
