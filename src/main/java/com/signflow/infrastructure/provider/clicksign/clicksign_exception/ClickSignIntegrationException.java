package com.signflow.infrastructure.provider.clicksign.clicksign_exception;

import com.signflow.infrastructure.exception.IntegrationException;
import lombok.Getter;

import java.util.List;

@Getter
public class ClickSignIntegrationException extends IntegrationException {

    private final List<ClickSignError> errors;

    public ClickSignIntegrationException(String message, List<ClickSignError> errors, String rawResponse) {
        super(message, rawResponse);
        this.errors = errors;
    }

    public ClickSignIntegrationException(String message, List<ClickSignError> errors, String rawResponse, Throwable cause) {
        super(message, rawResponse, cause);
        this.errors = errors;
    }
}
