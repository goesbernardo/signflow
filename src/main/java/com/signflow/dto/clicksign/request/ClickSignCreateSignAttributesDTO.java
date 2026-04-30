package com.signflow.dto.clicksign.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClickSignCreateSignAttributesDTO {

    private String name;
    private String email;
    private String documentation;
    @JsonProperty("has_documentation")
    private Boolean hasDocumentation;
    private Boolean refusable;
    private Integer group;
    @JsonProperty("location_required_enabled")
    private Boolean locationRequiredEnabled;
    @JsonProperty("communicate_events")
    private ClickSignCreateSignEventsDTO communicateEvents;



}
