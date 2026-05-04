package com.signflow.adapter.clicksign.mapper;

import com.signflow.adapter.clicksign.dto.ClickSignResponseAttributesDTO;
import com.signflow.adapter.clicksign.dto.ClickSignResponseDataDTO;
import com.signflow.adapter.clicksign.dto.SignatureClickSignResponseDTO;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ClickSignMapper {

    private static final Map<String, Status> STATUS_MAP = Map.of(
            "running", Status.ACTIVE,
            "completed", Status.CLOSED,
            "canceled", Status.CANCELED,
            "draft", Status.DRAFT
    );

    public Envelope toEnvelopeDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.data() == null) {
            return null;
        }
        ClickSignResponseDataDTO data = response.data();
        ClickSignResponseAttributesDTO attributes = data.attributes();

        Status domainStatus = null;
        if (attributes != null && attributes.status() != null) {
            String clickSignStatus = attributes.status().toLowerCase();
            domainStatus = STATUS_MAP.get(clickSignStatus);
            
            if (domainStatus == null) {
                log.warn("Status desconhecido recebido da ClickSign: {}. Mapeando para PENDING.", clickSignStatus);
                domainStatus = Status.PENDING;
            }
        }

        return Envelope.builder()
                .externalId(data.id())
                .name(attributes != null ? attributes.name() : null)
                .status(domainStatus)
                .created(attributes != null ? attributes.created() : null)
                .modified(attributes != null ? attributes.modified() : null)
                .build();
    }

    public Signer toSignerDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.data() == null) {
            return null;
        }
        ClickSignResponseDataDTO data = response.data();
        ClickSignResponseAttributesDTO attributes = data.attributes();

        return Signer.builder()
                .externalId(data.id())
                .name(attributes != null ? attributes.name() : null)
                .created(attributes != null ? attributes.created() : null)
                .modified(attributes != null ? attributes.modified() : null)
                .build();
    }

    public Document toDocumentDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.data() == null) {
            return null;
        }
        ClickSignResponseDataDTO data = response.data();
        ClickSignResponseAttributesDTO attributes = data.attributes();

        return Document.builder()
                .externalId(data.id())
                .created(attributes != null ? attributes.created() : null)
                .modified(attributes != null ? attributes.modified() : null)
                .build();
    }

    public Requirement toRequirementDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.data() == null) {
            return null;
        }
        ClickSignResponseDataDTO data = response.data();
        ClickSignResponseAttributesDTO attributes = data.attributes();

        return Requirement.builder()
                .externalId(data.id())
                .created(attributes != null ? attributes.created() : null)
                .modified(attributes != null ? attributes.modified() : null)
                .build();
    }
}
