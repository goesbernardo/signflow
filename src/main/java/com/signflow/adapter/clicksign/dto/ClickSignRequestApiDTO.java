package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClickSignRequestApiDTO<T>(T data) {

    // Sem id — usado em POST (criação)
    public static <A> ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<A, Void>> of(
            String type, A attributes) {
        return ClickSignRequestApiDTO.<ClickSignRequestApiDataDTO<A, Void>>builder()
                .data(ClickSignRequestApiDataDTO.<A, Void>builder()
                        .type(type)
                        .attributes(attributes)
                        .build())
                .build();
    }

    public static <A> ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<A, Void>> of(A attributes) {
        return ClickSignRequestApiDTO.<ClickSignRequestApiDataDTO<A, Void>>builder()
                .data(ClickSignRequestApiDataDTO.<A, Void>builder()
                        .attributes(attributes)
                        .build())
                .build();
    }



    // Com id — usado em PATCH/PUT (atualização/ativação)
    public static <A> ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<A, Void>> of(
            String id, String type, A attributes) {
        return ClickSignRequestApiDTO.<ClickSignRequestApiDataDTO<A, Void>>builder()
                .data(ClickSignRequestApiDataDTO.<A, Void>builder()
                        .id(id)
                        .type(type)
                        .attributes(attributes)
                        .build())
                .build();
    }

    // Com id e relationships — usado em operações complexas
    public static <A, R> ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<A, R>> of(
            String type, A attributes, R relationships) {
        return ClickSignRequestApiDTO.<ClickSignRequestApiDataDTO<A, R>>builder()
                .data(ClickSignRequestApiDataDTO.<A, R>builder()
                        .type(type)
                        .attributes(attributes)
                        .relationships(relationships)
                        .build())
                .build();
    }
}
