package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClickSignRequestApiDataDTO<A,R>{

    private String type;
    private A attributes;
    private R relationships;
}
