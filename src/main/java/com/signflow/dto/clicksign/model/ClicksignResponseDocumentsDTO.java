package com.signflow.dto.clicksign.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClicksignResponseDocumentsDTO {

    private List<ClicksignRelationDataDTO> data;



}
