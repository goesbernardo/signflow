package com.signflow.dto;

import lombok.Data;

@Data
public class ClickSignWebhookRequestAttributesDTO {

    private String url;
    private String events;
}
