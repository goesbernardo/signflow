package com.signflow.application.webhook;

public interface WebhookHandler {

    void handle(String provider, String payload);
}
