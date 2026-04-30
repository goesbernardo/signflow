package com.signflow.exception.clicksign;

import lombok.Getter;

import java.util.List;

@Getter
public class ClickSignIntegrationException extends RuntimeException {

    private final List<CLickSignError> errors;
    private final String rawResponse;

    public ClickSignIntegrationException(String message, List<CLickSignError> errors, String rawResponse) {
        super(message);
        this.errors = errors;
        this.rawResponse = rawResponse;
    }

    public ClickSignIntegrationException(String message, List<CLickSignError> errors, String rawResponse, Throwable cause) {
        super(message, cause);
        this.errors = errors;
        this.rawResponse = rawResponse;
    }
}
