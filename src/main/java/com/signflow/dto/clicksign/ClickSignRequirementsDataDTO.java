package com.signflow.dto.clicksign;

import lombok.Data;

@Data
public class ClickSignRequirementsDataDTO {

    private String type = "requirements";

    private ClickSignRequirementsAttributesDTO attributes;

    private ClickSignRequirimentsRelationshipDTO relationships;
}
