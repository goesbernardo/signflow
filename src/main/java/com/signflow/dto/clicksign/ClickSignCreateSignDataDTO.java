package com.signflow.dto.clicksign;

import lombok.Data;

@Data
public class ClickSignCreateSignDataDTO {

    private String type = "signers";
    private ClickSignCreateSignAttributesDTO attributes;

}
