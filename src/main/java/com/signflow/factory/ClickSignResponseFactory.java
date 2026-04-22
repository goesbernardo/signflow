package com.signflow.factory;

import com.signflow.dto.clicksign.ClickSignResponseAttributesDTO;
import com.signflow.dto.clicksign.ClickSignResponseDataDTO;
import com.signflow.dto.clicksign.SignatureClickSignResponseDTO;
import com.signflow.enums.Status;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ClickSignResponseFactory {

    public static SignatureClickSignResponseDTO pendingRetry() {
        return build(String.valueOf(Status.PENDING));
    }

    public static SignatureClickSignResponseDTO success() {
        return build(Status.SUCCESS.name());
    }

    public static SignatureClickSignResponseDTO failed() {
        return build(Status.FAILED.name());
    }

    private static SignatureClickSignResponseDTO build(String status) {

        ClickSignResponseAttributesDTO attributes = new ClickSignResponseAttributesDTO();
        attributes.setStatus(status);

        ClickSignResponseDataDTO data = new ClickSignResponseDataDTO();
        data.setAttributes(attributes);

        SignatureClickSignResponseDTO response = new SignatureClickSignResponseDTO();
        response.setData(data);

        return response;
    }


}
