package com.signflow.application.webhook;

import com.signflow.application.webhook.dto.WebhookReceivedEvent;
import com.signflow.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookConsumer {

    private final WebhookHandler webhookHandler;

    @KafkaListener(topics = KafkaConfig.WEBHOOK_RECEIVED_TOPIC, groupId = "signflow-webhook-processors")
    public void consume(WebhookReceivedEvent event) {
        log.info("Consumindo webhook do Kafka: provedor={}, payload_length={}", 
                event.getProvider(), event.getPayload().length());
        
        try {
            webhookHandler.handle(event.getProvider(), event.getPayload());
        } catch (Exception e) {
            log.error("Erro ao processar webhook do Kafka (provider={})", event.getProvider(), e);
            // Em produção, aqui poderíamos enviar para uma DLQ ou retentar
        }
    }
}
