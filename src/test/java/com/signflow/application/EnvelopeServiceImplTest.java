package com.signflow.application;

import com.signflow.adapter.ESignatureGateway;
import com.signflow.adapter.SignatureGatewayRegistry;
import com.signflow.domain.command.ActivateEnvelopeCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import com.signflow.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvelopeServiceImplTest {

    @Mock
    private SignatureGatewayRegistry registry;
    @Mock
    private SignatureRepository repository;
    @Mock
    private SignerRepository signerRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private RequirementRepository requirementRepository;
    @Mock
    private EnvelopeEventRepository eventRepository;
    @Mock
    private ESignatureGateway gateway;

    @InjectMocks
    private EnvelopeServiceImpl envelopeService;

    private String envelopeId = "env-123";
    private String signerId = "signer-123";
    private String documentId = "doc-123";

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldThrowExceptionWhenEnvelopeNotFound() {
        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.empty());

        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");

        assertThrows(RuntimeException.class, () -> 
            envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command)
        );
    }

    @Test
    void shouldThrowExceptionWhenSignerNotFound() {
        EnvelopeEntity envelope = new EnvelopeEntity();
        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelope));
        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.empty());

        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");

        assertThrows(RuntimeException.class, () -> 
            envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command)
        );
    }

    @Test
    void shouldThrowExceptionWhenDocumentNotFound() {
        EnvelopeEntity envelope = new EnvelopeEntity();
        SignerEntity signer = new SignerEntity();
        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelope));
        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.of(signer));
        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.empty());

        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");

        assertThrows(RuntimeException.class, () -> 
            envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command)
        );
    }

    @Test
    void shouldThrowExceptionWhenSignerDoesNotBelongToEnvelope() {
        EnvelopeEntity env1 = new EnvelopeEntity();
        env1.setId(1L);
        EnvelopeEntity env2 = new EnvelopeEntity();
        env2.setId(2L);

        SignerEntity signer = new SignerEntity();
        signer.setEnvelope(env2);

        DocumentEntity document = new DocumentEntity();
        document.setEnvelope(env1);

        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(env1));
        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.of(signer));
        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.of(document));

        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");

        assertThrows(RuntimeException.class, () -> 
            envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command)
        );
    }

    @Test
    void shouldActivateSuccessfully() {
        EnvelopeEntity envelope = new EnvelopeEntity();
        envelope.setId(1L);
        envelope.setStatus(Status.PROCESSING);

        SignerEntity signer = new SignerEntity();
        signer.setEnvelope(envelope);
        signer.setExternalId(signerId);

        DocumentEntity document = new DocumentEntity();
        document.setEnvelope(envelope);
        document.setExternalId(documentId);

        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelope));
        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.of(signer));
        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.of(document));
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);

        Requirement mockReq = Requirement.builder().externalId("req-abc").build();
        when(gateway.addRequirement(eq(envelopeId), any(AddRequirementCommand.class))).thenReturn(mockReq);

        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");

        envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command);

        verify(gateway, times(2)).addRequirement(eq(envelopeId), any(AddRequirementCommand.class));
        verify(gateway, times(1)).activateEnvelope(envelopeId);
        verify(repository, atLeastOnce()).save(envelope);
        verify(eventRepository, atLeastOnce()).save(any());
    }

    @Test
    void shouldAddMultipleSignersSuccessfully() {
        EnvelopeEntity envelope = new EnvelopeEntity();
        envelope.setExternalId(envelopeId);

        List<AddSignerCommand> commands = List.of(
                AddSignerCommand.builder().name("S1").email("s1@t.com").build(),
                AddSignerCommand.builder().name("S2").email("s2@t.com").build()
        );

        List<Signer> mockSigners = List.of(
                Signer.builder().externalId("ext-s1").name("S1").build(),
                Signer.builder().externalId("ext-s2").name("S2").build()
        );

        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        when(gateway.addSigners(envelopeId, commands)).thenReturn(mockSigners);
        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelope));

        List<Signer> result = envelopeService.addSigners(envelopeId, commands, ProviderSignature.CLICKSIGN);

        verify(gateway).addSigners(envelopeId, commands);
        verify(signerRepository, times(2)).save(any(SignerEntity.class));
        verify(repository).findByExternalId(envelopeId);
    }
}
