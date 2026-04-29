package com.signflow.dto;

import lombok.Data;

@Data
public class ClickSignWebhookAttributesResponseDTO {

    private String url;
    private String events;
    private String createdAt;
    private String updatedAt;

}
