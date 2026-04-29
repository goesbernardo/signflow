package com.signflow.dto;

import lombok.Data;

@Data
public class ClikSignWebhookResponseDataDTO {

    private String id;
    private String type;
    private ClickSignWebhookAttributesResponseDTO attributes;

}
