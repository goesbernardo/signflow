package com.signflow.adapter.clicksign.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClickSignRequirementsAttributesDTO {

    private String action;
}
