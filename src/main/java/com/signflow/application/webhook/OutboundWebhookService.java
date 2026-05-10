package com.signflow.application.webhook;

import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;

public interface OutboundWebhookService {
    void dispatchEvent(NormalizedWebhookEvent event);
    void dispatch(EnvelopeEntity envelope, NormalizedWebhookEvent event);
}
