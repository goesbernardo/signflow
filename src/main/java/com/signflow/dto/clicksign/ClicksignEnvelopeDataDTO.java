package com.signflow.dto.clicksign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClickSignEnvelopeDataDTO {


    private String type = "envelopes";
    private ClickSignEnvelopeAttributesDTO attributes;
    
}
