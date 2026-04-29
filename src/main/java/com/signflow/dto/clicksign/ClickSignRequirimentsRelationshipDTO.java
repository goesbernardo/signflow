package com.signflow.dto.clicksign;

import lombok.Data;

@Data
public class ClickSignRequirimentsRelationshipDTO {

    private DocumentDTO document;
    private SignerDTO signer;
}
