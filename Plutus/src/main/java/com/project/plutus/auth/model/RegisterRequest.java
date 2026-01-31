package com.project.plutus.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Value
@Builder
@AllArgsConstructor
public class RegisterRequest {
    String firstname;
    String lastname;
    String birthdate;
    String email;
    String password;
}
