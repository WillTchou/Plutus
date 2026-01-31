package com.project.plutus.exceptions;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ExceptionResponse {
    String code;
    String message;
}

