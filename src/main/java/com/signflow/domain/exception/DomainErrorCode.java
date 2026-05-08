package com.signflow.domain.exception;

import lombok.Getter;

@Getter
public enum DomainErrorCode {
    NOT_FOUND,
    INVALID_ENVELOPE_STATUS,
    REMINDER_RATE_LIMIT,
    INVALID_AUTH_METHOD,
    BUSINESS_RULE_VIOLATION
}
