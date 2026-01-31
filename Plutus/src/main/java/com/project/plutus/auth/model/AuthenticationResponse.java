package com.project.plutus.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.UUID;

@Data
@Value
@Builder
@AllArgsConstructor
public class AuthenticationResponse {
    String token;
    UUID userId;
}