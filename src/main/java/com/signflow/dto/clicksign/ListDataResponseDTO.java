package com.signflow.dto.clicksign;

import lombok.Data;

import java.util.List;

@Data
public class ListDataResponseDTO<T> {

    private List<T> data;
}
