package com.signflow.dto.clicksign.request;

import lombok.Data;

@Data
public class ClickSignRequirementsRelationshipDTO {

    private RelationshipDataDTO document;
    private RelationshipDataDTO signer;
}
