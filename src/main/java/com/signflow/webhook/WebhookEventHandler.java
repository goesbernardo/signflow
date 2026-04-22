package com.signflow.webhook;

import com.signflow.dto.clicksign.ClickSignWebhookDTO;

public interface WebhookEventHandler {

    String getEventType();
    void handleEvent(ClickSignWebhookDTO payload);
}
