package com.signflow.dto.clicksign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClickSignCreateSignDataDTO {

    private String type = "signers";
    private ClickSignCreateSignAttributesDTO attributes;


}
