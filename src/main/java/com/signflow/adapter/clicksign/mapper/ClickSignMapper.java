package com.signflow.adapter.clicksign.mapper;

import com.signflow.adapter.clicksign.dto.ClickSignResponseAttributesDTO;
import com.signflow.adapter.clicksign.dto.ClickSignResponseDataDTO;
import com.signflow.adapter.clicksign.dto.SignatureClickSignResponseDTO;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Signer;
import com.signflow.enums.Status;
import org.springframework.stereotype.Component;

@Component
public class ClickSignMapper {

    public Envelope toEnvelopeDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        ClickSignResponseDataDTO data = response.getData();
        ClickSignResponseAttributesDTO attributes = data.getAttributes();

        Status domainStatus = null;
        if (attributes != null && attributes.getStatus() != null) {
            try {
                domainStatus = Status.valueOf(attributes.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                domainStatus = Status.PENDING; // Fallback seguro
            }
        }

        return Envelope.builder()
                .externalId(data.getId())
                .name(attributes != null ? attributes.getName() : null)
                .status(domainStatus)
                .created(attributes != null ? attributes.getCreated() : null)
                .modified(attributes != null ? attributes.getModified() : null)
                .build();
    }

    public Signer toSignerDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        ClickSignResponseDataDTO data = response.getData();
        ClickSignResponseAttributesDTO attributes = data.getAttributes();

        return Signer.builder()
                .externalId(data.getId())
                .name(attributes != null ? attributes.getName() : null)
                .created(attributes != null ? attributes.getCreated() : null)
                .modified(attributes != null ? attributes.getModified() : null)
                .build();
    }

    public Document toDocumentDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        ClickSignResponseDataDTO data = response.getData();
        ClickSignResponseAttributesDTO attributes = data.getAttributes();

        return Document.builder()
                .externalId(data.getId())
                .created(attributes != null ? attributes.getCreated() : null)
                .modified(attributes != null ? attributes.getModified() : null)
                .build();
    }
}
