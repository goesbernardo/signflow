package com.signflow.exception.domain;

import lombok.Getter;

@Getter
public class IntegrationException extends DomainException {
    private final String rawResponse;

    public IntegrationException(String message, String rawResponse) {
        super(message);
        this.rawResponse = rawResponse;
    }

    public IntegrationException(String message, String rawResponse, Throwable cause) {
        super(message, cause);
        this.rawResponse = rawResponse;
    }
}
