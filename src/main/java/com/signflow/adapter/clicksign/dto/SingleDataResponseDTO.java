package com.signflow.adapter.clicksign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleDataResponseDTO<T> {

    private T data;

}
