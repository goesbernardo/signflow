package com.signflow.dto.clicksign.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClicksignResponseSignersDTO {

    private List<ClicksignRelationDataDTO> data;

}
