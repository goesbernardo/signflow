package com.signflow.exception.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErroDetail {

    private String field;
    private String message;
    private String code;
}
