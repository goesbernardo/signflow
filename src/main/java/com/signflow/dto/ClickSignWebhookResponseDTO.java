package com.signflow.dto;

import com.signflow.dto.clicksign.ClickSignResponseDataDTO;
import lombok.Data;

@Data
public class ClickSignWebhookResponseDTO {

    private ClickSignResponseDataDTO data;
}
