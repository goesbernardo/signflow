package com.signflow.webhook;

import com.signflow.dto.clicksign.ClickSignWebhookDTO;
import com.signflow.enums.Status;
import com.signflow.repository.ClickSignDocumentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DocumentFinishedHandler implements WebhookEventHandler{

    private final ClickSignDocumentRepository clickSignDocumentRepository;

    public DocumentFinishedHandler(ClickSignDocumentRepository clickSignDocumentRepository) {
        this.clickSignDocumentRepository = clickSignDocumentRepository;
    }

    @Override
    public String getEventType() {
        return "document.finished";
    }

    @Override
    public void handleEvent(ClickSignWebhookDTO payload) {
        String requestId = payload.getData().getMetadata().getRequestId();
        clickSignDocumentRepository.findById(String.valueOf(UUID.fromString(requestId))).ifPresent(envelopeEntity -> {
            envelopeEntity.setStatus(Status.SUCCESS);
            envelopeEntity.setCreated(LocalDateTime.now());
            clickSignDocumentRepository.save(envelopeEntity);
        });


    }
}
