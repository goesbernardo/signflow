package com.signflow.dto.clicksign.response;

import lombok.Data;

@Data
public class SingleDataResponseDTO<T> {

    private T data;

}
