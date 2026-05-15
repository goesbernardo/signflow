package com.signflow.infrastructure.provider.docusign.mapper;

import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.Status;
import com.signflow.infrastructure.provider.docusign.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DocuSignMapper {

    private static final Map<String, Status> STATUS_MAP = Map.of(
            "created",   Status.DRAFT,
            "sent",      Status.ACTIVE,
            "delivered", Status.ACTIVE,
            "signed",    Status.PENDING,
            "completed", Status.CLOSED,
            "declined",  Status.REFUSED,
            "voided",    Status.CANCELED,
            "deleted",   Status.DELETED
    );

    public Envelope toEnvelopeDomain(DocuSignEnvelopeResponseDTO response) {
        if (response == null) return null;

        Status domainStatus = null;
        if (response.status() != null) {
            String dsStatus = response.status().toLowerCase();
            domainStatus = STATUS_MAP.get(dsStatus);
            if (domainStatus == null) {
                log.warn("Status desconhecido recebido do DocuSign: {}. Mapeando para PENDING.", dsStatus);
                domainStatus = Status.PENDING;
            }
        }

        return Envelope.builder()
                .externalId(response.envelopeId())
                .name(response.emailSubject())
                .status(domainStatus)
                .created(parseOffsetDateTime(response.createdDateTime()))
                .provider("DOCUSIGN")
                .build();
    }

    public Signer toSignerDomain(DocuSignSignerResponseDTO dto) {
        if (dto == null) return null;
        return Signer.builder()
                .externalId(dto.recipientId())
                .name(dto.name())
                .email(dto.email())
                .status(dto.status())
                .signedAt(parseOffsetDateTime(dto.signedDateTime()))
                .created(parseOffsetDateTime(dto.createdDateTime()))
                .modified(parseOffsetDateTime(dto.deliveredDateTime()))
                .build();
    }

    public List<Signer> toSignerListDomain(DocuSignRecipientsResponseDTO response) {
        if (response == null || response.signers() == null) return List.of();
        return response.signers().stream()
                .map(this::toSignerDomain)
                .toList();
    }

    public Document toDocumentDomain(DocuSignDocumentResponseDTO dto) {
        if (dto == null) return null;
        return Document.builder()
                .externalId(dto.documentId())
                .build();
    }

    public List<Document> toDocumentListDomain(DocuSignDocumentsListDTO response) {
        if (response == null || response.envelopeDocuments() == null) return List.of();
        return response.envelopeDocuments().stream()
                .map(this::toDocumentDomain)
                .toList();
    }

    public Requirement toRequirementDomain(DocuSignTabsResponseDTO response) {
        if (response == null) return null;

        String tabId = response.tabId();
        if (tabId == null && response.signHereTabs() != null && !response.signHereTabs().isEmpty()) {
            tabId = response.signHereTabs().get(0).tabId();
        }
        if (tabId == null && response.initialHereTabs() != null && !response.initialHereTabs().isEmpty()) {
            tabId = response.initialHereTabs().get(0).tabId();
        }

        return Requirement.builder()
                .externalId(tabId)
                .build();
    }

    private OffsetDateTime parseOffsetDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        try {
            return OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            log.warn("Não foi possível parsear data DocuSign: {}", dateTimeStr);
            return null;
        }
    }

    public LocalDateTime parseLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return LocalDateTime.now();
        try {
            return OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
