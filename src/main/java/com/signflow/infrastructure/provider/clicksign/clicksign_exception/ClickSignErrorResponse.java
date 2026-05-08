package com.signflow.infrastructure.provider.clicksign.clicksign_exception;

import lombok.Data;

import java.util.List;

@Data
public class ClickSignErrorResponse {

    private List<ClickSignError> errors;


}
