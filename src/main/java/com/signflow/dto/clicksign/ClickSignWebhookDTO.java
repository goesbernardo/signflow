package com.signflow.dto.clicksign;

import lombok.Data;

@Data
public class ClickSignWebhookDTO {

    private ClickSignWebhookDataDTO data;
    private String event;
}
