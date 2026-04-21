package com.signflow.dto.clicksign;

import lombok.Data;

@Data
public class SignatureClickSignDocumentListResponseAttributesDTO {

    private String status;
    private String filename;
    private String template;
    private String created;
    private String modified;
}
