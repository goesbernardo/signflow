package com.signflow.api;

import com.signflow.adapter.clicksign.client.ClickSignIntegrationFeignClient;
import com.signflow.adapter.clicksign.dto.*;
import com.signflow.domain.command.CreateWhatsAppAcceptanceCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
@Tag(name = "Aceite via WhatsApp", description = "Criação de aceites via WhatsApp na ClickSign")
public class WhatsAppAcceptanceController {

    private final ClickSignIntegrationFeignClient clickSignClient;

    @PostMapping("/acceptance")
    @Operation(summary = "Criar Aceite via WhatsApp", description = """
            Cria um aceite via WhatsApp na ClickSign.
             O destinatário receberá uma mensagem no WhatsApp com o conteúdo
            do aceite e poderá confirmar ou recusar diretamente pelo aplicativo.
             **Fluxo independente** — não requer envelope, documento ou signatário.
             **sender_name_option:**
            - `user_name` → exibe só o nome do usuário (ex: "Maria")
            - `account_name` → exibe só o nome da conta (ex: "Minha Empresa")
            - `user_and_account_name` → exibe ambos (ex: "Maria (Minha Empresa)")
            """
    )
    public ResponseEntity<WhatsAppAcceptanceResponse> createAcceptance(@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject("""
                    {
                      "title": "Alteração de Plano",
                      "senderNameOption": "user_and_account_name",
                      "senderPhone": "5521999999999",
                      "message": "Eu, João Silva, declaro que aceito os termos da alteração de plano para R$ 129,99/mês.",
                      "signerPhone": "5521988888888",
                      "signerName": "João Silva"
                    }
                    """))) @RequestBody @Valid CreateWhatsAppAcceptanceCommand command) {

        log.info("Criando aceite via WhatsApp para: {}", command.signerName());

        ClickSignWhatsAppAcceptanceAttributesDTO attributes =
                ClickSignWhatsAppAcceptanceAttributesDTO.builder()
                        .title(command.title())
                        .senderNameOption(command.senderNameOption())
                        .senderPhone(command.senderPhone())
                        .message(command.message())
                        .signerPhone(command.signerPhone())
                        .signerName(command.signerName())
                        .build();

        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignWhatsAppAcceptanceAttributesDTO, Void>> body = ClickSignRequestApiDTO.of("acceptance_term_whatsapps", attributes);

        ClickSignWhatsAppAcceptanceResponseDTO clickSignResponse = clickSignClient.createWhatsAppAcceptance(body);

        log.info("Aceite via WhatsApp criado: id={}, status={}", clickSignResponse.data().id(), clickSignResponse.data().attributes().status());

        String status = clickSignResponse.data().attributes().status();

        WhatsAppAcceptanceResponse response = WhatsAppAcceptanceResponse.builder()
                .externalId(clickSignResponse.data().id())
                .title(clickSignResponse.data().attributes().title())
                .signerPhone(clickSignResponse.data().attributes().signerPhone())
                .status(status)
                .statusDescription(WhatsAppAcceptanceResponse.describeStatus(status))
                .createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
