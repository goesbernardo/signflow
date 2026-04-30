package com.signflow.dto.clicksign.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClickSignRequestApiDataDTO<A,R>{

    private String type;
    private A attributes;
    private R relationships;
}
