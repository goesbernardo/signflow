package com.signflow.infrastructure.exception;

import com.signflow.domain.exception.DomainException;

public class InvalidRequestException extends DomainException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
