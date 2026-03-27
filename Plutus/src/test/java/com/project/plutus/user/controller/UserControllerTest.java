package com.project.plutus.user.controller;

import com.project.plutus.user.model.KycState;
import com.project.plutus.user.model.UserDTO;
import com.project.plutus.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String EMAIL = "user@plutus.com";

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @Test
    void getAuthedUser_returnsDto() {
        when(authentication.getName()).thenReturn(EMAIL);
        UserDTO dto = new UserDTO(UUID.randomUUID(), "Jane", "Doe", EMAIL, KycState.NOT_VERIFIED);
        when(userService.getAuthedUser(EMAIL)).thenReturn(dto);

        ResponseEntity<UserDTO> response = userController.getAuthedUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(userService).getAuthedUser(EMAIL);
    }

    @Test
    void verifyUser_returnsNoContent() {
        when(authentication.getName()).thenReturn(EMAIL);

        ResponseEntity<Void> response = userController.verifyUser(authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).verifyUser(EMAIL);
    }
}
