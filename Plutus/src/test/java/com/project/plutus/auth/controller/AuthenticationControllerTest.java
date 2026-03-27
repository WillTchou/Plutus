package com.project.plutus.auth.controller;

import com.project.plutus.auth.model.AuthenticationRequest;
import com.project.plutus.auth.model.AuthenticationResponse;
import com.project.plutus.auth.model.RegisterRequest;
import com.project.plutus.auth.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void register_delegatesToService() {
        RegisterRequest request = RegisterRequest.builder()
                .firstname("Jane")
                .lastname("Doe")
                .birthdate("1990-01-01")
                .email("jane@plutus.com")
                .password("plain")
                .build();
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("jwt-token")
                .userId(UUID.randomUUID())
                .build();

        when(authenticationService.register(request)).thenReturn(response);

        ResponseEntity<AuthenticationResponse> result = authenticationController.register(request);

        assertEquals(response, result.getBody());
        verify(authenticationService).register(request);
    }

    @Test
    void authenticate_delegatesToService() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("jane@plutus.com")
                .password("plain")
                .build();
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("jwt-token")
                .userId(UUID.randomUUID())
                .build();

        when(authenticationService.authenticate(request)).thenReturn(response);

        ResponseEntity<AuthenticationResponse> result = authenticationController.authenticate(request);

        assertEquals(response, result.getBody());
        verify(authenticationService).authenticate(request);
    }
}
