package com.signflow.dto.clicksign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SignatureClickSignResponseAttributesDTO {

    private String name;
    private String birthday;
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("location_required_enabled")
    private Boolean locationRequiredEnabled;

    @JsonProperty("has_documentation")
    private Boolean hasDocumentation;

    private String documentation;

    private Boolean refusable;
    private Integer group;

    @JsonProperty("communicate_events")
    private ClickSignCreateSignEventsDTO communicateEvents;

    private String created;
    private String modified;
}

