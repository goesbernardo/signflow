package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private String name;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
    private OffsetDateTime created;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
    private OffsetDateTime modified;

    @JsonProperty("birthday")
    private String birthday;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("has_documentation")
    private Boolean hasDocumentation;

    @JsonProperty("documentation")
    private String documentation;

    @JsonProperty("deadline_at")
    private String deadline;

    private String locale;

    @JsonProperty("auto_close")
    private Boolean autoClose;

    @JsonProperty("remind_interval")
    private Integer remindInterval;

    @JsonProperty("sequence_enabled")
    private Boolean sequenceEnabled;

}
