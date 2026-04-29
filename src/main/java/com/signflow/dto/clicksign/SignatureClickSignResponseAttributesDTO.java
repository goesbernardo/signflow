package com.signflow.dto.clicksign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SignatureClickSignResponseAttributesDTO {

    private String name;
    @JsonProperty("communicate_events")
    private ClickSignCreateSignEventsDTO communicateEvents;

    private String created;
    private String modified;


}

