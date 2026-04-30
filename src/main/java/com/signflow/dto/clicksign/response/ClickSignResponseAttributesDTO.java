package com.signflow.dto.clicksign.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClickSignResponseAttributesDTO {

    private String status;
    private OffsetDateTime created;
    private OffsetDateTime modified;



}
