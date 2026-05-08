package com.signflow.application;

import com.signflow.application.port.out.ESignatureGateway;
import com.signflow.application.service.impl.SignatureServiceImpl;
import com.signflow.domain.command.*;
import com.signflow.infrastructure.gateway.SignatureGatewayRegistry;
import com.signflow.infrastructure.persistence.entity.DocumentEntity;
import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;
import com.signflow.infrastructure.persistence.entity.RequirementEntity;
import com.signflow.infrastructure.persistence.entity.SignerEntity;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.RequirementRole;
import com.signflow.enums.Status;
import com.signflow.infrastructure.persistence.repository.*;

import com.signflow.domain.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignatureServiceImplTest {

    @Mock
    private SignatureGatewayRegistry registry;
    @Mock
    private EnvelopeRepository repository;
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
    private SignatureServiceImpl envelopeService;

    private String envelopeId = "env-123";
    private String signerId = "signer-123";
    private String documentId = "doc-123";

    @BeforeEach
    void setUp() {
    }

//    @Test
//    void shouldThrowExceptionWhenEnvelopeNotFound() {
//        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.empty());
//
//        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");
//
//        assertThrows(RuntimeException.class, () ->
//            envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command)
//        );
//    }
//
//    @Test
//    void shouldThrowExceptionWhenSignerNotFound() {
//        EnvelopeEntity envelope = new EnvelopeEntity();
//        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelope));
//        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.empty());
//
//        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");
//
//        assertThrows(RuntimeException.class, () ->
//            envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command)
//        );
//    }

//    @Test
//    void shouldThrowExceptionWhenDocumentNotFound() {
//        EnvelopeEntity envelope = new EnvelopeEntity();
//        SignerEntity signer = new SignerEntity();
//        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelope));
//        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.of(signer));
//        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.empty());
//
//        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");
//
//        assertThrows(RuntimeException.class, () ->
//            envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command)
//        );
//    }

//    @Test
//    void shouldThrowExceptionWhenSignerDoesNotBelongToEnvelope() {
//        EnvelopeEntity env1 = new EnvelopeEntity();
//        env1.setId(1L);
//        EnvelopeEntity env2 = new EnvelopeEntity();
//        env2.setId(2L);
//
//        SignerEntity signer = new SignerEntity();
//        signer.setEnvelope(env2);
//
//        DocumentEntity document = new DocumentEntity();
//        document.setEnvelope(env1);
//
//        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(env1));
//        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.of(signer));
//        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.of(document));
//
//        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");
//
//        assertThrows(RuntimeException.class, () ->
//            envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command)
//        );
//    }

//    @Test
//    void shouldActivateSuccessfully() {
//        EnvelopeEntity envelope = new EnvelopeEntity();
//        envelope.setId(1L);
//        envelope.setStatus(Status.PROCESSING);
//
//        SignerEntity signer = new SignerEntity();
//        signer.setEnvelope(envelope);
//        signer.setExternalId(signerId);
//
//        DocumentEntity document = new DocumentEntity();
//        document.setEnvelope(envelope);
//        document.setExternalId(documentId);
//
//        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelope));
//        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.of(signer));
//        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.of(document));
//        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
//
//        Requirement mockReq = Requirement.builder().externalId("req-abc").build();
//        when(gateway.addRequirement(eq(envelopeId), any(AddRequirementCommand.class))).thenReturn(mockReq);
//
//        ActivateEnvelopeCommand command = new ActivateEnvelopeCommand(signerId, documentId, "sign", "email");
//
//        envelopeService.activateEnvelopeComplete(ProviderSignature.CLICKSIGN, envelopeId, command);
//
//        verify(gateway, times(2)).addRequirement(eq(envelopeId), any(AddRequirementCommand.class));
//        verify(gateway, times(1)).activateEnvelope(envelopeId);
//        verify(repository, atLeastOnce()).save(envelope);
//        verify(eventRepository, atLeastOnce()).save(any());
//    }

    @Test
    void shouldAddMultipleSignersSuccessfully() {
        EnvelopeEntity envelope = new EnvelopeEntity();
        envelope.setExternalId(envelopeId);

        List<AddSignerCommand> commands = List.of(
                AddSignerCommand.builder().name("S1").email("s1@t.com").phoneNumber("5511999999999").build(),
                AddSignerCommand.builder().name("S2").email("s2@t.com").phoneNumber("5511988888888").build()
        );

        List<Signer> mockSigners = List.of(
                Signer.builder().externalId("ext-s1").name("S1").build(),
                Signer.builder().externalId("ext-s2").name("S2").build()
        );

        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        // O EnvelopeServiceImpl chama addSigners (default na interface) que por sua vez chama addSigner N vezes.
        when(gateway.addSigner(eq(envelopeId), any(AddSignerCommand.class)))
                .thenReturn(mockSigners.get(0))
                .thenReturn(mockSigners.get(1));
        
        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelope));

        List<Signer> result = envelopeService.addSigners(envelopeId, commands, ProviderSignature.CLICKSIGN);

        verify(gateway, times(2)).addSigner(eq(envelopeId), any(AddSignerCommand.class));
        verify(signerRepository, times(2)).save(any(SignerEntity.class));
        verify(repository).findByExternalId(envelopeId);
    }

    @Test
    void shouldCreateFullEnvelopeSuccessfully() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("test-user");
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        CreateFullEnvelopeCommand cmd = CreateFullEnvelopeCommand.builder()
                .name("Full")
                .documents(List.of(AddDocumentCommand.builder().filename("d.pdf").build()))
                .signers(List.of(AddSignerCommand.builder().name("S").phoneNumber("5511999999999").build()))
                .autoActivate(true)
                .build();

        EnvelopeEntity envelopeEntity = new EnvelopeEntity();
        envelopeEntity.setId(1L);
        envelopeEntity.setExternalId(envelopeId);

        Envelope mockEnv = Envelope.builder().externalId(envelopeId).name("Full").status(Status.PROCESSING).build();
        Document mockDoc = Document.builder().externalId(documentId).build();
        List<Signer> mockSigners = List.of(Signer.builder().externalId(signerId).build());

        lenient().when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        
        // Mock createEnvelope
        lenient().when(gateway.createEnvelope(any(CreateEnvelopeCommand.class))).thenReturn(mockEnv);
        lenient().when(repository.save(any(EnvelopeEntity.class))).thenReturn(envelopeEntity);
        
        // Mock addDocument
        lenient().when(gateway.addDocument(eq(envelopeId), any(AddDocumentCommand.class))).thenReturn(mockDoc);
        lenient().when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelopeEntity));
        
        // Mock addSigners
        lenient().when(gateway.addSigner(eq(envelopeId), any(AddSignerCommand.class))).thenReturn(mockSigners.get(0));
        
        // Mock getEnvelope (at the end of createFullEnvelope)
        lenient().when(gateway.getEnvelope(envelopeId)).thenReturn(mockEnv);

        try {
            Envelope result = envelopeService.createFullEnvelope(cmd, ProviderSignature.CLICKSIGN);

            verify(gateway).createEnvelope(any());
            verify(gateway).addDocument(eq(envelopeId), any());
            verify(gateway).addSigner(eq(envelopeId), any());
            verify(gateway).activateEnvelope(envelopeId);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
    @Test
    void shouldCreateFullEnvelopeWithRubricRequirement() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("test-user");
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        CreateFullEnvelopeCommand cmd = CreateFullEnvelopeCommand.builder()
                .name("Full with Rubric")
                .documents(List.of(AddDocumentCommand.builder().filename("d.pdf").build()))
                .signers(List.of(AddSignerCommand.builder().name("S").phoneNumber("5511999999999").build()))
                .requirements(List.of(FullRequirementCommand.builder()
                        .role(RequirementRole.SIGN)
                        .rubricPages("1,2,3")
                        .build()))
                .autoActivate(true)
                .build();

        EnvelopeEntity envelopeEntity = new EnvelopeEntity();
        envelopeEntity.setId(1L);
        envelopeEntity.setExternalId(envelopeId);

        Envelope mockEnv = Envelope.builder().externalId(envelopeId).name("Full").status(Status.PROCESSING).build();
        Document mockDoc = Document.builder().externalId(documentId).build();
        List<Signer> mockSigners = List.of(Signer.builder().externalId(signerId).build());

        lenient().when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        lenient().when(gateway.createEnvelope(any())).thenReturn(mockEnv);
        lenient().when(repository.save(any())).thenReturn(envelopeEntity);
        lenient().when(gateway.addDocument(eq(envelopeId), any())).thenReturn(mockDoc);
        lenient().when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(envelopeEntity));
        lenient().when(gateway.addSigner(eq(envelopeId), any(AddSignerCommand.class))).thenReturn(mockSigners.get(0));
        lenient().when(gateway.getEnvelope(envelopeId)).thenReturn(mockEnv);

        try {
            envelopeService.createFullEnvelope(cmd, ProviderSignature.CLICKSIGN);

            // O EnvelopeServiceImpl chama addRequirement que recebe 3 parametros,
            // mas internamente chama gateway.addRequirement que recebe 2 parametros.
            // Aqui estamos verificando a chamada no MOCK do gateway.
            verify(gateway).addRequirement(eq(envelopeId), any(AddRequirementCommand.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldCancelEnvelopeSuccessfully() {
        EnvelopeEntity entity = new EnvelopeEntity();
        entity.setExternalId(envelopeId);
        entity.setStatus(Status.ACTIVE);

        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(entity));
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);

        envelopeService.cancelEnvelope(envelopeId, ProviderSignature.CLICKSIGN);

        assertEquals(Status.CANCELED, entity.getStatus());
        verify(gateway).cancelEnvelope(envelopeId);
        verify(repository).save(entity);
        verify(eventRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCancellingEnvelopeWithInvalidStatus() {
        EnvelopeEntity entity = new EnvelopeEntity();
        entity.setExternalId(envelopeId);
        entity.setStatus(Status.CLOSED);

        when(repository.findByExternalId(envelopeId)).thenReturn(Optional.of(entity));

        DomainException exception = assertThrows(DomainException.class, () ->
                envelopeService.cancelEnvelope(envelopeId, ProviderSignature.CLICKSIGN)
        );

        assertEquals("Somente envelopes ACTIVE ou DRAFT podem ser cancelados. Status atual: CLOSED", exception.getMessage());
        verifyNoInteractions(registry);
    }

    @Test
    void shouldGetDocumentsSuccessfully() {
        when(documentRepository.findAllByEnvelopeExternalId(envelopeId)).thenReturn(List.of(new DocumentEntity()));

        List<Document> result = envelopeService.getDocuments(envelopeId, ProviderSignature.CLICKSIGN);

        assertEquals(1, result.size());
        verify(documentRepository).findAllByEnvelopeExternalId(envelopeId);
    }

    @Test
    void shouldGetDocumentSuccessfully() {
        DocumentEntity entity = new DocumentEntity();
        entity.setExternalId(documentId);
        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.of(entity));

        Document result = envelopeService.getDocument(documentId, ProviderSignature.CLICKSIGN);

        assertEquals(documentId, result.getExternalId());
        verify(documentRepository).findByExternalId(documentId);
    }

    @Test
    void shouldUpdateDocumentSuccessfully() {
        DocumentEntity entity = new DocumentEntity();
        entity.setExternalId(documentId);
        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.of(entity));

        UpdateDocumentCommand cmd = new UpdateDocumentCommand("new-name.pdf");
        Document result = envelopeService.updateDocument(documentId, cmd, ProviderSignature.CLICKSIGN);

        assertEquals("new-name.pdf", entity.getFilename());
        verify(documentRepository).save(entity);
    }

    @Test
    void shouldDeleteDocumentSuccessfully() {
        DocumentEntity entity = new DocumentEntity();
        entity.setExternalId(documentId);
        when(documentRepository.findByExternalId(documentId)).thenReturn(Optional.of(entity));

        envelopeService.deleteDocument(documentId, ProviderSignature.CLICKSIGN);

        verify(documentRepository).delete(entity);
    }

    @Test
    void shouldGetSignersSuccessfully() {
        when(signerRepository.findAllByEnvelopeExternalId(envelopeId)).thenReturn(List.of(new SignerEntity()));

        List<Signer> result = envelopeService.getSigners(envelopeId, ProviderSignature.CLICKSIGN);

        assertEquals(1, result.size());
        verify(signerRepository).findAllByEnvelopeExternalId(envelopeId);
    }

    @Test
    void shouldGetSignerSuccessfully() {
        SignerEntity entity = new SignerEntity();
        entity.setExternalId(signerId);
        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.of(entity));

        Signer result = envelopeService.getSigner(envelopeId, signerId, ProviderSignature.CLICKSIGN);

        assertEquals(signerId, result.getExternalId());
        verify(signerRepository).findByExternalId(signerId);
    }

    @Test
    void shouldDeleteSignerSuccessfully() {
        SignerEntity entity = new SignerEntity();
        entity.setExternalId(signerId);
        when(signerRepository.findByExternalId(signerId)).thenReturn(Optional.of(entity));

        envelopeService.deleteSigner(envelopeId, signerId, ProviderSignature.CLICKSIGN);

        verify(signerRepository).delete(entity);
    }

    @Test
    void shouldGetRequirementsSuccessfully() {
        when(requirementRepository.findAllByEnvelopeExternalId(envelopeId)).thenReturn(List.of(new RequirementEntity()));

        List<Requirement> result = envelopeService.getRequirements(envelopeId, ProviderSignature.CLICKSIGN);

        assertEquals(1, result.size());
        verify(requirementRepository).findAllByEnvelopeExternalId(envelopeId);
    }

    @Test
    void shouldGetRequirementSuccessfully() {
        RequirementEntity entity = new RequirementEntity();
        entity.setExternalId("req-123");
        when(requirementRepository.findByExternalId("req-123")).thenReturn(Optional.of(entity));

        Requirement result = envelopeService.getRequirement("req-123", ProviderSignature.CLICKSIGN);

        assertEquals("req-123", result.getExternalId());
        verify(requirementRepository).findByExternalId("req-123");
    }

    @Test
    void shouldDeleteRequirementSuccessfully() {
        RequirementEntity entity = new RequirementEntity();
        entity.setExternalId("req-123");
        when(requirementRepository.findByExternalId("req-123")).thenReturn(Optional.of(entity));

        envelopeService.deleteRequirement("req-123", ProviderSignature.CLICKSIGN);

        verify(requirementRepository).delete(entity);
    }
}
