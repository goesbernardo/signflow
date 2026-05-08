package com.signflow.adapter.clicksign.mapper;

import com.signflow.adapter.clicksign.dto.ClickSignResponseAttributesDTO;
import com.signflow.adapter.clicksign.dto.ClickSignResponseDataDTO;
import com.signflow.adapter.clicksign.dto.SignatureClickSignListResponseDTO;
import com.signflow.adapter.clicksign.dto.SignatureClickSignResponseDTO;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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
        return toEnvelopeDomain(response.data());
    }

    private Envelope toEnvelopeDomain(ClickSignResponseDataDTO data) {
        if (data == null) return null;
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
                .build();
    }

    public Signer toSignerDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.data() == null) {
            return null;
        }
        return toSignerDomain(response.data());
    }

    private Signer toSignerDomain(ClickSignResponseDataDTO data) {
        if (data == null) return null;
        ClickSignResponseAttributesDTO attributes = data.attributes();

        return Signer.builder()
                .externalId(data.id())
                .name(attributes != null ? attributes.name() : null)
                .created(attributes != null ? attributes.created() : null)
                .modified(attributes != null ? attributes.modified() : null)
                .build();
    }
    
    public List<Signer> toSignerListDomain(SignatureClickSignListResponseDTO response) {
        if (response == null || response.data() == null) {
            return List.of();
        }
        return response.data().stream()
                .map(this::toSignerDomain)
                .toList();
    }

    public List<Signer> toSignerListDomainFromSingleResponse(SignatureClickSignResponseDTO response) {
        if (response == null || response.data() == null) {
            return List.of();
        }
        // As vezes a Clicksign retorna uma lista dentro do campo data mesmo no SignatureClickSignResponseDTO se o JSON for um array
        // Mas o Feign geralmente mapeia para o record. 
        // Se getEnvelopeSigners retornar um objeto cujo 'data' é uma lista, o SignatureClickSignResponseDTO pode falhar se data for declarado como objeto unico.
        return List.of(toSignerDomain(response.data()));
    }

    public Document toDocumentDomain(SignatureClickSignResponseDTO response) {
        if (response == null || response.data() == null) {
            return null;
        }
        return toDocumentDomain(response.data());
    }

    public List<Document> toDocumentListDomain(SignatureClickSignListResponseDTO response) {
        if (response == null || response.data() == null) {
            return List.of();
        }
        return response.data().stream()
                .map(this::toDocumentDomain)
                .toList();
    }

    private Document toDocumentDomain(ClickSignResponseDataDTO data) {
        if (data == null) return null;
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
        return toRequirementDomain(response.data());
    }

    public List<Requirement> toRequirementListDomain(SignatureClickSignListResponseDTO response) {
        if (response == null || response.data() == null) {
            return List.of();
        }
        return response.data().stream()
                .map(this::toRequirementDomain)
                .toList();
    }

    private Requirement toRequirementDomain(ClickSignResponseDataDTO data) {
        if (data == null) return null;
        ClickSignResponseAttributesDTO attributes = data.attributes();

        return Requirement.builder()
                .externalId(data.id())
                .created(attributes != null ? attributes.created() : null)
                .modified(attributes != null ? attributes.modified() : null)
                .build();
    }
}
