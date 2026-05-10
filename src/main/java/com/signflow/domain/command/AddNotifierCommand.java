package com.signflow.domain.command;

import lombok.Builder;

@Builder
public record AddNotifierCommand(
        String email,
        String name
) {
}
