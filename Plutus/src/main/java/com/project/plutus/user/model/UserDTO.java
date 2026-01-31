package com.project.plutus.user.model;

import java.util.UUID;

public record UserDTO(UUID id,
                      String firstname,
                      String lastname,
                      String email,
                      KycState kycState) {
}
