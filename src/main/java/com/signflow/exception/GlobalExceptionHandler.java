package com.signflow.exception;

import com.signflow.adapter.clicksign.exception.ClickSignIntegrationException;
import com.signflow.exception.domain.ErroDetail;
import com.signflow.exception.domain.ErrorResponse;
import com.signflow.exception.domain.IntegrationException;
import com.signflow.exception.domain.InvalidRequestException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Requisição Inválida")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrationError(IntegrationException ex, HttpServletRequest request) {
        List<ErroDetail> details = null;

        if (ex instanceof ClickSignIntegrationException clickSignEx && clickSignEx.getErrors() != null) {
            details = clickSignEx.getErrors().stream()
                    .map(error -> ErroDetail.builder()
                            .code(error.getCode())
                            .message(error.getDetail())
                            .field(error.getSource() != null ? error.getSource().getPointer() : null)
                            .build())
                    .toList();
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Erro de integração")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimiter(RequestNotPermitted ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Limite de requisições excedido")
                .message("Você atingiu o limite de requisições permitidas. Por favor, tente novamente em breve.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }
}
