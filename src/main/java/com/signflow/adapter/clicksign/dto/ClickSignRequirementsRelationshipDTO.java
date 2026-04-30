package com.signflow.adapter.clicksign.dto;

import lombok.Data;

@Data
public class ClickSignRequirementsRelationshipDTO {

    private RelationshipDataDTO document;
    private RelationshipDataDTO signer;
}
