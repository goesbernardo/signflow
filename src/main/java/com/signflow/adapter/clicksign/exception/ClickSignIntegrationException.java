package com.signflow.adapter.clicksign.exception;

import com.signflow.exception.domain.IntegrationException;
import lombok.Getter;

import java.util.List;

@Getter
public class ClickSignIntegrationException extends IntegrationException {

    private final List<CLickSignError> errors;

    public ClickSignIntegrationException(String message, List<CLickSignError> errors, String rawResponse) {
        super(message, rawResponse);
        this.errors = errors;
    }

    public ClickSignIntegrationException(String message, List<CLickSignError> errors, String rawResponse, Throwable cause) {
        super(message, rawResponse, cause);
        this.errors = errors;
    }
}
