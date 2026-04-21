package com.signflow.dto.clicksign;

import lombok.Data;

@Data
public class ClickSignCreateRequestRelationshipDTO {

    private ClickSignCreateDocumentDTO document;
    private ClickSignCreateSignerDTO signer;


}
