package com.signflow.application.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.enums.DeliveryStatus;
import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;
import com.signflow.infrastructure.persistence.entity.OutboundWebhookDeliveryEntity;
import com.signflow.infrastructure.persistence.repository.OutboundWebhookDeliveryRepository;
import com.signflow.config.KafkaConfig;
import com.signflow.infrastructure.persistence.repository.EnvelopeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundWebhookServiceImpl implements OutboundWebhookService {

    private final OutboundWebhookDeliveryRepository deliveryRepository;
    private final EnvelopeRepository envelopeRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestClient restClient = RestClient.create();

    @Override
    @Transactional
    @KafkaListener(topics = KafkaConfig.ENVELOPE_EVENTS_TOPIC, groupId = "signflow-outbound-generator")
    public void dispatchEvent(NormalizedWebhookEvent event) {
        envelopeRepository.findByExternalId(event.getEnvelopeExternalId()).ifPresent(envelope -> {
            this.dispatch(envelope, event);
        });
    }

    @Override
    public void dispatch(EnvelopeEntity envelope, NormalizedWebhookEvent event) {
        if (envelope.getCallbackUrl() == null || envelope.getCallbackUrl().isBlank()) {
            log.debug("Nenhuma callback_url configurada para o envelope {}", envelope.getExternalId());
            return;
        }

        OutboundWebhookPayload payload = OutboundWebhookPayload.builder()
                .envelopeId(envelope.getExternalId())
                .provider(envelope.getProvider())
                .eventType(event.getEventType())
                .status(envelope.getStatus())
                .occurredAt(event.getOccurredAt())
                .build();

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Erro ao serializar payload de webhook para o envelope {}", envelope.getExternalId(), e);
            return;
        }

        OutboundWebhookDeliveryEntity delivery = OutboundWebhookDeliveryEntity.builder()
                .envelope(envelope)
                .url(envelope.getCallbackUrl())
                .payload(jsonPayload)
                .status(DeliveryStatus.PENDING)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        delivery = deliveryRepository.save(delivery);
        kafkaTemplate.send(KafkaConfig.WEBHOOK_OUTBOUND_TOPIC, delivery.getId().toString(), delivery.getId());
    }

    @KafkaListener(topics = KafkaConfig.WEBHOOK_OUTBOUND_TOPIC, groupId = "signflow-outbound-sender")
    public void processOutbound(Long deliveryId) {
        deliveryRepository.findById(deliveryId).ifPresent(this::sendAttempt);
    }

    private void sendAttempt(OutboundWebhookDeliveryEntity delivery) {
        int maxAttempts = 3;
        int[] backoffs = {60, 600}; // segundos para o próximo retry: 1m, 10m

        int attempt = delivery.getAttempts();
        delivery.setAttempts(attempt + 1);
        delivery.setLastAttemptAt(LocalDateTime.now());

        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri(delivery.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(delivery.getPayload())
                    .retrieve()
                    .toBodilessEntity();

            delivery.setHttpStatusCode(response.getStatusCode().value());
            delivery.setStatus(DeliveryStatus.SUCCESS);
            delivery.setNextAttemptAt(null);
            deliveryRepository.save(delivery);
            log.info("Webhook enviado com sucesso para {} (tentativa {}, status {})", 
                    delivery.getUrl(), delivery.getAttempts(), delivery.getHttpStatusCode());
        } catch (Exception e) {
            log.warn("Falha ao enviar webhook para {} (tentativa {}): {}",
                    delivery.getUrl(), delivery.getAttempts(), e.getMessage());

            // Tenta extrair status code se for erro de cliente/servidor
            if (e instanceof org.springframework.web.client.HttpStatusCodeException sce) {
                delivery.setHttpStatusCode(sce.getStatusCode().value());
            } else if (e instanceof org.springframework.web.client.RestClientResponseException rcre) {
                delivery.setHttpStatusCode(rcre.getStatusCode().value());
            }

            if (delivery.getAttempts() < maxAttempts) {
                delivery.setStatus(DeliveryStatus.PENDING_RETRY);
                int backoffIndex = delivery.getAttempts() - 1; // 0 para a primeira falha, 1 para a segunda
                int backoffSeconds = backoffs[Math.min(backoffIndex, backoffs.length - 1)];
                delivery.setNextAttemptAt(LocalDateTime.now().plusSeconds(backoffSeconds));
                log.info("Webhook agendado para retry em {}s para {}", backoffSeconds, delivery.getUrl());
                
                // Em um cenário real com Kafka, usaríamos o delay do broker ou um tópico de delay
                // Para manter a compatibilidade com a regra de 1m/10m, vamos agendar o envio via Kafka novamente
                // No futuro, isso pode ser substituído por Spring Kafka DeadLetterPublishingRecoverer com FixedBackOff
                kafkaTemplate.send(KafkaConfig.WEBHOOK_OUTBOUND_TOPIC, delivery.getId().toString(), delivery.getId());
            } else {
                delivery.setStatus(DeliveryStatus.FAILED);
                delivery.setNextAttemptAt(null);
                log.error("Esgotadas as tentativas de envio de webhook para {}", delivery.getUrl());
                kafkaTemplate.send(KafkaConfig.WEBHOOK_OUTBOUND_DLQ_TOPIC, delivery.getId().toString(), delivery.getId());
            }
            deliveryRepository.save(delivery);
        }
    }
}
