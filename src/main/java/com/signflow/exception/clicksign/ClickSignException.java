package com.signflow.exception.clicksign;

public class ClickSignException extends RuntimeException {

    public ClickSignException(String message) {
        super(message);
    }

    public ClickSignException(String message, Throwable cause) {
        super(message, cause);
    }
}
