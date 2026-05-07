package com.signflow.application;

import com.signflow.adapter.ESignatureGateway;
import com.signflow.adapter.SignatureGatewayRegistry;
import com.signflow.domain.command.ActivateEnvelopeCommand;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.UpdateDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.CreateFullEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import com.signflow.domain.command.FullRequirementCommand;
import com.signflow.enums.RequirementAction;
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
                .signers(List.of(AddSignerCommand.builder().name("S").build()))
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
        lenient().when(gateway.addSigners(eq(envelopeId), any(List.class))).thenReturn(mockSigners);
        
        // Mock getEnvelope (at the end of createFullEnvelope)
        lenient().when(gateway.getEnvelope(envelopeId)).thenReturn(mockEnv);

        try {
            Envelope result = envelopeService.createFullEnvelope(cmd, ProviderSignature.CLICKSIGN);

            verify(gateway).createEnvelope(any());
            verify(gateway).addDocument(eq(envelopeId), any());
            verify(gateway).addSigners(eq(envelopeId), any());
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
                .signers(List.of(AddSignerCommand.builder().name("S").build()))
                .requirements(List.of(FullRequirementCommand.builder()
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
        lenient().when(gateway.addSigners(eq(envelopeId), any())).thenReturn(mockSigners);
        lenient().when(gateway.getEnvelope(envelopeId)).thenReturn(mockEnv);

        try {
            envelopeService.createFullEnvelope(cmd, ProviderSignature.CLICKSIGN);

            verify(gateway).addRequirement(eq(envelopeId), argThat(req -> 
                req.action() == RequirementAction.AGREE && 
                "1,2,3".equals(req.rubricPages())
            ));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldGetDocumentsSuccessfully() {
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        when(gateway.getDocuments(envelopeId)).thenReturn(List.of(Document.builder().externalId(documentId).build()));

        List<Document> result = envelopeService.getDocuments(envelopeId, ProviderSignature.CLICKSIGN);

        assertEquals(1, result.size());
        assertEquals(documentId, result.get(0).getExternalId());
        verify(gateway).getDocuments(envelopeId);
    }

    @Test
    void shouldGetDocumentSuccessfully() {
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        when(gateway.getDocument(documentId)).thenReturn(Document.builder().externalId(documentId).build());

        Document result = envelopeService.getDocument(documentId, ProviderSignature.CLICKSIGN);

        assertEquals(documentId, result.getExternalId());
        verify(gateway).getDocument(documentId);
    }

    @Test
    void shouldUpdateDocumentSuccessfully() {
        UpdateDocumentCommand cmd = new UpdateDocumentCommand("new-name.pdf");
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        when(gateway.updateDocument(documentId, cmd)).thenReturn(Document.builder().externalId(documentId).build());

        Document result = envelopeService.updateDocument(documentId, cmd, ProviderSignature.CLICKSIGN);

        assertEquals(documentId, result.getExternalId());
        verify(gateway).updateDocument(documentId, cmd);
    }

    @Test
    void shouldDeleteDocumentSuccessfully() {
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);

        envelopeService.deleteDocument(documentId, ProviderSignature.CLICKSIGN);

        verify(gateway).deleteDocument(documentId);
    }

    @Test
    void shouldGetSignersSuccessfully() {
        String envId = "env-123";
        List<Signer> signers = List.of(Signer.builder().externalId("signer-1").build());
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        when(gateway.getSigners(envId)).thenReturn(signers);

        List<Signer> result = envelopeService.getSigners(envId, ProviderSignature.CLICKSIGN);

        assertEquals(1, result.size());
        assertEquals("signer-1", result.get(0).getExternalId());
    }

    @Test
    void shouldGetSignerSuccessfully() {
        String envId = "env-123";
        String sId = "signer-1";
        Signer signer = Signer.builder().externalId(sId).build();
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);
        when(gateway.getSigner(envId, sId)).thenReturn(signer);

        Signer result = envelopeService.getSigner(envId, sId, ProviderSignature.CLICKSIGN);

        assertEquals(sId, result.getExternalId());
    }

    @Test
    void shouldDeleteSignerSuccessfully() {
        String envId = "env-123";
        String sId = "signer-1";
        when(registry.get(ProviderSignature.CLICKSIGN)).thenReturn(gateway);

        envelopeService.deleteSigner(envId, sId, ProviderSignature.CLICKSIGN);

        verify(gateway).deleteSigner(envId, sId);
    }

    @Test
    void shouldGetRequirementsSuccessfully() {
        ProviderSignature provider = ProviderSignature.CLICKSIGN;
        Requirement requirement = Requirement.builder().externalId("req-123").build();
        when(registry.get(provider)).thenReturn(gateway);
        when(gateway.getRequirements(envelopeId)).thenReturn(List.of(requirement));

        List<Requirement> result = envelopeService.getRequirements(envelopeId, provider);

        assertEquals(1, result.size());
        assertEquals("req-123", result.get(0).getExternalId());
    }

    @Test
    void shouldGetRequirementSuccessfully() {
        ProviderSignature provider = ProviderSignature.CLICKSIGN;
        String requirementId = "req-123";
        Requirement requirement = Requirement.builder().externalId(requirementId).build();
        when(registry.get(provider)).thenReturn(gateway);
        when(gateway.getRequirement(requirementId)).thenReturn(requirement);

        Requirement result = envelopeService.getRequirement(requirementId, provider);

        assertEquals(requirementId, result.getExternalId());
    }

    @Test
    void shouldDeleteRequirementSuccessfully() {
        ProviderSignature provider = ProviderSignature.CLICKSIGN;
        String requirementId = "req-123";
        when(registry.get(provider)).thenReturn(gateway);

        envelopeService.deleteRequirement(requirementId, provider);

        verify(gateway).deleteRequirement(requirementId);
        verify(requirementRepository).deleteByExternalId(requirementId);
    }
}
