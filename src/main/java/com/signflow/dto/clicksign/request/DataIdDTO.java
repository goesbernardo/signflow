package com.signflow.dto.clicksign.request;

import lombok.Data;

@Data
public class DataIdDTO {

    private String type; // "documents" ou "signers"
    private String id;
}
