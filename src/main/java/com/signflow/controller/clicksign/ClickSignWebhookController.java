package com.signflow.controller.clicksign;

import com.signflow.dto.clicksign.ClickSignCreateEnvelopeRequestDTO;
import com.signflow.dto.clicksign.ClickSignWebhookDTO;
import com.signflow.service.ClickSignSignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/clicksign")
@Slf4j
@RequiredArgsConstructor
public class ClickSignWebhookController {


    private final ClickSignSignatureService clickSignSignatureService;

    @PostMapping
    public ResponseEntity<?> handle(@RequestBody ClickSignWebhookDTO payload){

        return ResponseEntity.ok().build();

    }





}
