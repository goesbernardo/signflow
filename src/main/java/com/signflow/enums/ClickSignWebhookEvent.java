package com.signflow.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClickSignWebhookEvent {

    UPLOAD("upload"),
    CLOSE("close"),
    AUTO_CLOSE("auto_close"),
    DEADLINE("deadline"),
    DOCUMENT_CLOSED("document_closed"),
    CANCEL("cancel"),

    ADD_SIGNER("add_signer"),
    REMOVE_SIGNER("remove_signer"),
    SIGN("sign"),
    SIGNATURE_STARTED("signature_started"),
    REFUSAL("refusal"),

    ATTEMPTS_BY_WHATSAPP_EXCEEDED("attempts_by_whatsapp_exceeded"),
    ATTEMPTS_BY_LIVENESS_OR_FACEMATCH_EXCEEDED("attempts_by_liveness_or_facematch_exceeded"),
    LIVENESS_REFUSED("liveness_refused"),
    FACEMATCH_REFUSED("facematch_refused"),
    BIOMETRIC_REFUSED("biometric_refused"),
    DOCUMENTSCOPY_REFUSED("documentscopy_refused"),
    OCR_REFUSED("ocr_refused"),

    UPDATE_DEADLINE("update_deadline"),
    UPDATE_AUTO_CLOSE("update_auto_close"),
    UPDATE_LOCALE("update_locale"),
    CUSTOM("custom"),

    ACCEPTANCE_TERM_ENQUEUED("acceptance_term_enqueued"),
    ACCEPTANCE_TERM_SENT("acceptance_term_sent"),
    ACCEPTANCE_TERM_COMPLETED("acceptance_term_completed"),
    ACCEPTANCE_TERM_REFUSED("acceptance_term_refused"),
    ACCEPTANCE_TERM_CANCELED("acceptance_term_canceled"),
    ACCEPTANCE_TERM_EXPIRED("acceptance_term_expired"),
    ACCEPTANCE_TERM_ERROR("acceptance_term_error"),

    UNKNOWN("unknown");

    @JsonValue
    private final String value;

    @JsonCreator
    public static ClickSignWebhookEvent fromValue(String value) {
        if (value == null) return UNKNOWN;
        for (ClickSignWebhookEvent e : values()) {
            if (e.value.equalsIgnoreCase(value)) return e;
        }
        return UNKNOWN;
    }
}
