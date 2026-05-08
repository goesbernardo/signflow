package com.signflow.domain.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {
    private final DomainErrorCode errorCode;

    public DomainException(DomainErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(DomainErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public DomainException(String message) {
        super(message);
        this.errorCode = DomainErrorCode.BUSINESS_RULE_VIOLATION;
    }
}
