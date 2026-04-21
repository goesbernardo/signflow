package com.signflow.dto.clicksign;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClickSignResponseDocumentsDTO {

    private List<ClickSignRelationResponseDataDTO> data;



}
