package com.signflow.exception.clicksign;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErroDetail {

    private String field;
    private String message;
    private String code;
}
