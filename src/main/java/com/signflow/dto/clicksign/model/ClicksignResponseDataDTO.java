package com.signflow.dto.clicksign.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClicksignResponseDataDTO {

    private String id;
    private String type;
    private ClicksignResponseAttributesDTO attributes;
}
