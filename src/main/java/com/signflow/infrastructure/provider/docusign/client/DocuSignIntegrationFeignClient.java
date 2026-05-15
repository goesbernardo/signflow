package com.signflow.infrastructure.provider.docusign.client;

import com.signflow.config.DocuSignFeignConfig;
import com.signflow.infrastructure.provider.docusign.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "docusign-client",
        url = "${signflow.providers.docusign.base-url}",
        configuration = DocuSignFeignConfig.class
)
public interface DocuSignIntegrationFeignClient {

    // ── Envelopes ─────────────────────────────────────────────────────────

    @PostMapping("/envelopes")
    DocuSignEnvelopeResponseDTO createEnvelope(@RequestBody DocuSignCreateEnvelopeDTO request);

    @GetMapping("/envelopes/{envelopeId}")
    DocuSignEnvelopeResponseDTO getEnvelope(@PathVariable String envelopeId);

    @PutMapping("/envelopes/{envelopeId}")
    DocuSignEnvelopeResponseDTO updateEnvelope(@PathVariable String envelopeId, @RequestBody DocuSignUpdateEnvelopeDTO request);

    // ── Documents ─────────────────────────────────────────────────────────

    @GetMapping("/envelopes/{envelopeId}/documents")
    DocuSignDocumentsListDTO getDocuments(@PathVariable String envelopeId);

    @PutMapping("/envelopes/{envelopeId}/documents")
    DocuSignEnvelopeResponseDTO addDocuments(@PathVariable String envelopeId, @RequestBody DocuSignDocumentsUpdateDTO request);

    /** Download do PDF assinado. Retorna bytes do documento. */
    @GetMapping(value = "/envelopes/{envelopeId}/documents/{documentId}", produces = "application/pdf")
    byte[] downloadDocument(@PathVariable String envelopeId, @PathVariable String documentId);

    /** Download do Certificate of Completion (audit trail em PDF). */
    @GetMapping(value = "/envelopes/{envelopeId}/documents/certificate", produces = "application/pdf")
    byte[] downloadCertificate(@PathVariable String envelopeId);

    // ── Recipients ────────────────────────────────────────────────────────

    @GetMapping("/envelopes/{envelopeId}/recipients")
    DocuSignRecipientsResponseDTO getRecipients(@PathVariable String envelopeId);

    @PostMapping("/envelopes/{envelopeId}/recipients")
    DocuSignRecipientsResponseDTO addRecipients(@PathVariable String envelopeId, @RequestBody DocuSignRecipientsDTO request);

    @PutMapping("/envelopes/{envelopeId}/recipients")
    DocuSignRecipientsResponseDTO resendToRecipients(@PathVariable String envelopeId, @RequestParam("resend_envelope") boolean resend, @RequestBody DocuSignRecipientsDTO request);

    @PostMapping("/envelopes/{envelopeId}/recipients")
    DocuSignRecipientsResponseDTO addCarbonCopies(@PathVariable String envelopeId, @RequestBody DocuSignRecipientsDTO request);

    // ── Tabs ──────────────────────────────────────────────────────────────

    @PostMapping("/envelopes/{envelopeId}/recipients/{recipientId}/tabs")
    DocuSignTabsResponseDTO addTabs(@PathVariable String envelopeId, @PathVariable String recipientId, @RequestBody DocuSignTabDTO request);

    // ── Views — Embedded Signing ──────────────────────────────────────────

    /**
     * Gera URL de embedded signing para um signatário específico.
     * A URL é válida por 5 minutos.
     * POST /envelopes/{id}/views/recipient
     */
    @PostMapping("/envelopes/{envelopeId}/views/recipient")
    DocuSignViewResponseDTO createRecipientView(@PathVariable String envelopeId, @RequestBody DocuSignRecipientViewRequestDTO request);

    /**
     * Gera URL de embedded sender view (configuração de envelope dentro do sistema).
     * POST /envelopes/{id}/views/sender
     */
    @PostMapping("/envelopes/{envelopeId}/views/sender")
    DocuSignViewResponseDTO createSenderView(@PathVariable String envelopeId, @RequestBody DocuSignSenderViewRequestDTO request);

    // ── Audit Trail ───────────────────────────────────────────────────────

    /** Retorna todos os eventos de auditoria do envelope (IP, device, timestamp). */
    @GetMapping("/envelopes/{envelopeId}/audit_events")
    DocuSignAuditEventsResponseDTO getAuditEvents(@PathVariable String envelopeId);
}
