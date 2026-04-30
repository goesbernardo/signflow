package com.signflow.adapter.clicksign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClickSignResponseDataDTO extends BaseDataDTO {

    private ClickSignResponseAttributesDTO attributes;
}
