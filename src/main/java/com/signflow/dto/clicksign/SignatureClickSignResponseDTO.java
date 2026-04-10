package com.signflow.dto.clicksign;

import com.signflow.dto.clicksign.model.ClicksignResponseDataDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignatureClickSignResponseDTO {

    private ClicksignResponseDataDTO data;

}
