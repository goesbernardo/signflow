package com.signflow.client.clicksign;

import com.signflow.config.feign.ClickSignFeignConfig;
import com.signflow.dto.clicksign.ClickSignCreateEnvelopeRequestDTO;
import com.signflow.dto.clicksign.ClickSignSendDocumentsRequestDTO;
import com.signflow.dto.clicksign.SignatureClickSignResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "clicksign-client", url = "${clicksign.api.url}", configuration = ClickSignFeignConfig.class)
public interface ClickSignClient {

    @PostMapping(value = "envelopes")
    SignatureClickSignResponseDTO createEnvelope(@RequestBody ClickSignCreateEnvelopeRequestDTO request);
    @PostMapping(value = "/envelopes/{envelope_id}/documents")
    SignatureClickSignResponseDTO sendDocuments(@PathVariable("envelope_id") String envelopeId, @RequestBody ClickSignSendDocumentsRequestDTO request);

}
