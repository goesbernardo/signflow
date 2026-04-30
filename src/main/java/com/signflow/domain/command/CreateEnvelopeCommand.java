package com.signflow.domain.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateEnvelopeCommand {
    private String name;
}
