package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSignerCommand {
    private String name;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "000.000.000-00")
    private String documentation;
    private Boolean hasDocumentation;
    private String phone;
    private String delivery; // email, sms, whatsapp
    private String requestSignature;  //email , whatsapp, sms
}
