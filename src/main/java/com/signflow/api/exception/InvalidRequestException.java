package com.signflow.exception;

public class InvalidRequestException extends DomainException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
