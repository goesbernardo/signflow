package com.signflow.dto.clicksign;

import lombok.Data;

@Data
public class SignatureClickSignRequirementDataDTO extends BaseDataDTO {

    private SignatureClickSignRequirementAttributesResponseDTO attributes;
    private SignatureClickSignRequirementRelationshipsDTO relationships;


}
