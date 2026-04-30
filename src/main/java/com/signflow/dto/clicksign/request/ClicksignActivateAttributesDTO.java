package com.signflow.dto.clicksign.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClicksignActivateAttributesDTO {

    private String status;
    private String name;
    @JsonProperty("deadline_at")
    private String deadlineAt;
    private String locale;
    @JsonProperty("auto_close")
    private boolean autoClose;
    @JsonProperty("default_message")
    private String defaultMessage;
}
