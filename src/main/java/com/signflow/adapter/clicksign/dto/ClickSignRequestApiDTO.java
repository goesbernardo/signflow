package com.signflow.adapter.clicksign.dto;

import lombok.Data;

@Data
public class ClickSignRequestApiDTO<T> {

    private T data;

    public static <A> ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<A, Void>> of(String type, A attributes) {
        var dataDTO = new ClickSignRequestApiDataDTO<A, Void>();
        dataDTO.setType(type);
        dataDTO.setAttributes(attributes);

        var dto = new ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<A, Void>>();
        dto.setData(dataDTO);
        return dto;
    }

    public static <A, R> ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<A, R>> of(String type, A attributes, R relationships) {
        var dataDTO = new ClickSignRequestApiDataDTO<A, R>();
        dataDTO.setType(type);
        dataDTO.setAttributes(attributes);
        dataDTO.setRelationships(relationships);

        var dto = new ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<A, R>>();
        dto.setData(dataDTO);
        return dto;
    }
}
