package com.signflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String WEBHOOK_RECEIVED_TOPIC = "signflow.webhook.received";
    public static final String ENVELOPE_EVENTS_TOPIC = "signflow.envelope.events";
    public static final String WEBHOOK_OUTBOUND_TOPIC = "signflow.webhook.outbound";
    public static final String WEBHOOK_OUTBOUND_DLQ_TOPIC = "signflow.webhook.outbound.dlq";

    @Bean
    public NewTopic webhookReceivedTopic() {
        return TopicBuilder.name(WEBHOOK_RECEIVED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic envelopeEventsTopic() {
        return TopicBuilder.name(ENVELOPE_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic webhookOutboundTopic() {
        return TopicBuilder.name(WEBHOOK_OUTBOUND_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic webhookOutboundDlqTopic() {
        return TopicBuilder.name(WEBHOOK_OUTBOUND_DLQ_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
