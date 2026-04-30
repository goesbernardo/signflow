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

    public Envelope toDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        ClickSignResponseDataDTO data = response.getData();
        ClickSignResponseAttributesDTO attributes = data.getAttributes();

        return Envelope.builder()
                .externalId(data.getId())
                .status(attributes != null && attributes.getStatus() != null ? Status.valueOf(attributes.getStatus().toUpperCase()) : null)
                .created(attributes != null ? attributes.getCreated() : null)
                .modified(attributes != null ? attributes.getModified() : null)
                .build();
    }

    public Signer toSignerDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        return Signer.builder()
                .externalId(response.getData().getId())
                .build();
    }

    public Document toDocumentDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        return Document.builder()
                .externalId(response.getData().getId())
                .build();
    }
}
