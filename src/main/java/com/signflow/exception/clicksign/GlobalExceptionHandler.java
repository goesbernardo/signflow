package com.signflow.exception.clicksign;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClickSignIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrationError(ClickSignIntegrationException ex, HttpServletRequest request) {

        List<ErroDetail> details = ex.getErrors() != null
                ? ex.getErrors().stream()
                  .map(err -> ErroDetail.builder()
                              .field(err.getSource() != null ? err.getSource().toString() : null)
                              .message(err.getDetail())
                              .code(err.getCode())
                              .build())
                  .toList()
                : List.of();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erro de integração")
                .message(ex.getMessage())
                .details(details)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
