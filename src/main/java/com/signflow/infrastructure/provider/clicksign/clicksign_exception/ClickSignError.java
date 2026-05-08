package com.signflow.infrastructure.provider.clicksign.clicksign_exception;

import lombok.Data;


@Data
public class ClickSignError {

    private String code;
    private Integer status;
    private String detail;
    private Source source;
}
