package com.signflow.application.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventProcessor {



    @Async
    @Transactional
    public void process(NormalizedWebhookEvent event) {

        switch (event.getEventType()) {

            case DOCUMENT_SIGNED:
                handleSigned(event);
                break;

            case DOCUMENT_REJECTED:
                handleRejected(event);
                break;

            case DOCUMENT_COMPLETED:
                handleCompleted(event);
                break;

            default:
                log.warn(
                        "Unhandled event: {}",
                        event.getEventType()
                );
        }
    }

    private void handleSigned(NormalizedWebhookEvent event) {

    }

    private void handleRejected(NormalizedWebhookEvent event) {

    }

    private void handleCompleted(NormalizedWebhookEvent event) {

    }
}
