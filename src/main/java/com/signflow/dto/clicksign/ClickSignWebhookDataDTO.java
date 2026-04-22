package com.signflow.dto.clicksign;

import com.signflow.dto.ClickSignWebhookMetadataDTO;
import lombok.Data;

@Data
public class ClickSignWebhookDataDTO {

    private String type = "envelopes";
    private String id;
    private ClickSignWebhookAttributesDTO attributes;
    private ClickSignWebhookMetadataDTO metadata;
}
