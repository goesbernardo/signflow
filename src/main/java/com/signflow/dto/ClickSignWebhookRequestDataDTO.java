package com.signflow.dto;

import lombok.Data;

@Data
public class ClickSignWebhookRequestDataDTO {

    private String type = "webhooks";
    private ClickSignWebhookRequestAttributesDTO attributes;
}
