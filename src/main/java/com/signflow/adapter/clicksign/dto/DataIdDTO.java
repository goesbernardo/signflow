package com.signflow.adapter.clicksign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataIdDTO {

    private String type; // "documents" ou "signers"
    private String id;
}
