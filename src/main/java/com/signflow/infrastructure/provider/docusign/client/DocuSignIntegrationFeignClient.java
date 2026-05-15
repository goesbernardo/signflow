package com.signflow.infrastructure.provider.docusign.client;

import com.signflow.config.DocuSignFeignConfig;
import com.signflow.infrastructure.provider.docusign.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "docusign-client", url = "${signflow.providers.docusign.base-url}", configuration = DocuSignFeignConfig.class
)
public interface DocuSignIntegrationFeignClient {

    @PostMapping("/envelopes")
    DocuSignEnvelopeResponseDTO createEnvelope(@RequestBody DocuSignCreateEnvelopeDTO request);

    @GetMapping("/envelopes/{envelopeId}")
    DocuSignEnvelopeResponseDTO getEnvelope(@PathVariable String envelopeId);

    @PutMapping("/envelopes/{envelopeId}")
    DocuSignEnvelopeResponseDTO updateEnvelope(@PathVariable String envelopeId, @RequestBody DocuSignUpdateEnvelopeDTO request);

    @GetMapping("/envelopes/{envelopeId}/recipients")
    DocuSignRecipientsResponseDTO getRecipients(@PathVariable String envelopeId);

    @PostMapping("/envelopes/{envelopeId}/recipients")
    DocuSignRecipientsResponseDTO addRecipients(@PathVariable String envelopeId, @RequestBody DocuSignRecipientsDTO request);

    @PutMapping("/envelopes/{envelopeId}/recipients")
    DocuSignRecipientsResponseDTO resendToRecipients(@PathVariable String envelopeId, @RequestParam("resend_envelope") boolean resend, @RequestBody DocuSignRecipientsDTO request);

    @GetMapping("/envelopes/{envelopeId}/documents")
    DocuSignDocumentsListDTO getDocuments(@PathVariable String envelopeId);

    @PutMapping("/envelopes/{envelopeId}/documents")
    DocuSignEnvelopeResponseDTO addDocuments(@PathVariable String envelopeId, @RequestBody DocuSignDocumentsUpdateDTO request);

    @PostMapping("/envelopes/{envelopeId}/recipients/{recipientId}/tabs")
    DocuSignTabsResponseDTO addTabs(@PathVariable String envelopeId, @PathVariable String recipientId, @RequestBody DocuSignTabDTO request);

    @PostMapping("/envelopes/{envelopeId}/recipients")
    DocuSignRecipientsResponseDTO addCarbonCopies(@PathVariable String envelopeId, @RequestBody DocuSignRecipientsDTO request);
}
