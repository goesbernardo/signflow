package com.signflow.adapter.clicksign.exception;

import lombok.Data;

import java.util.List;

@Data
public class ClickSignErrorResponse {

    private List<CLickSignError> errors;


}
