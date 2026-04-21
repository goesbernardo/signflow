package com.signflow.dto.clicksign;

import lombok.Data;

@Data
public class SingleDataResponseDTO<T> {

    private T data;
}
