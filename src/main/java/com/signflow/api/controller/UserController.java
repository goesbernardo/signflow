package com.signflow.api.controller;

import com.signflow.application.port.in.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
@Tag(name = "User Management", description = "Endpoints for managing users and privacy")
public class UserController {

    private final SignatureService signatureService;

    @DeleteMapping("/me")
    @Operation(summary = "Excluir minha conta (LGPD)", 
               description = "Realiza o soft delete dos dados do usuário em conformidade com o direito ao esquecimento da LGPD.")
    public ResponseEntity<Void> deleteMe() {
        log.info("Recebendo solicitação de exclusão de conta.");
        signatureService.deleteMe();
        return ResponseEntity.noContent().build();
    }
}
