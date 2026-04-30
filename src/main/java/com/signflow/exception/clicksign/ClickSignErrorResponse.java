package com.signflow.exception.clicksign;

import lombok.Data;

import java.util.List;

@Data
public class ClickSignErrorResponse {

    private List<CLickSignError> errors;


}
