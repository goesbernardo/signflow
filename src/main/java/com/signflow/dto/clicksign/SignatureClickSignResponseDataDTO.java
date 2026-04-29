package com.signflow.dto.clicksign;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SignatureClickSignResponseDataDTO extends BaseDataDTO {

    private SignatureClickSignResponseAttributesDTO attributes;
}
