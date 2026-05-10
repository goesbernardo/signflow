package com.signflow.api.controller;

import com.signflow.api.dto.LoginRequest;
import com.signflow.api.dto.LoginResponse;
import com.signflow.application.service.AuditLogService;
import com.signflow.config.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para gestão de acesso e tokens")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;

    @Operation(summary = "Realizar login", description = "Autentica um usuário e retorna um token JWT para acesso às APIs protegidas.")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(schema = @Schema(implementation = LoginResponse.class), examples = @ExampleObject(value = "{ \"token\": \"eyJhbGciOiJIUzI1NiJ9...\" }")))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciais de acesso", content = @Content(examples = @ExampleObject(value = "{ \"username\": \"admin\", \"password\": \"admin123\" }")))
            @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        final String jwt = jwtUtils.generateToken(userDetails);
        log.info("Autenticação com a clicksign efetuada com sucesso: {}"  , jwt);

        auditLogService.log(request.username(), "LOGIN", "USER", request.username(), "Login realizado com sucesso", null, null);

        return ResponseEntity.ok(LoginResponse.builder().token(jwt).build());
    }
}
