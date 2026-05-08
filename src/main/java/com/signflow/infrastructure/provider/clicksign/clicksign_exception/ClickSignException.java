package com.signflow.infrastructure.provider.clicksign.exception;

public class ClickSignException extends RuntimeException {

    public ClickSignException(String message) {
        super(message);
    }

    public ClickSignException(String message, Throwable cause) {
        super(message, cause);
    }
}
