package com.signflow.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class IntegrationException extends DomainException {
    private final String rawResponse;
    private final List<ErroDetail> details;

    public IntegrationException(String message, String rawResponse) {
        this(message, rawResponse, null, null);
    }

    public IntegrationException(String message, String rawResponse, List<ErroDetail> details) {
        this(message, rawResponse, details, null);
    }

    public IntegrationException(String message, String rawResponse, Throwable cause) {
        this(message, rawResponse, null, cause);
    }

    public IntegrationException(String message, String rawResponse, List<ErroDetail> details, Throwable cause) {
        super(message, cause);
        this.rawResponse = rawResponse;
        this.details = details;
    }
}
