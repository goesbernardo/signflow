package com.signflow.exception.clicksign;

import lombok.Data;


@Data
public class CLickSignError {

    private String code;
    private Integer status;
    private String detail;
    private Source source;
}
