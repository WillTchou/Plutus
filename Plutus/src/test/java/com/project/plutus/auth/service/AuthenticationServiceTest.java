package com.project.plutus.auth.service;

import com.project.plutus.auth.model.AuthenticationRequest;
import com.project.plutus.auth.model.AuthenticationResponse;
import com.project.plutus.auth.model.RegisterRequest;
import com.project.plutus.user.model.Role;
import com.project.plutus.user.model.User;
import com.project.plutus.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String EMAIL = "jane@plutus.com";
    private static final String PASSWORD = "plain";
    private static final String FIRSTNAME = "Jane";
    private static final String LASTNAME = "Doe";
    private static final String TOKEN = "jwt-token";
    private static final String BIRTH_DATE = "1990-01-01";

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_createsUser_andReturnsToken() {
        RegisterRequest request = RegisterRequest.builder()
                .firstname(FIRSTNAME)
                .lastname(LASTNAME)
                .birthdate(BIRTH_DATE)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        String encoded = "encoded-pass";
        String token = TOKEN;
        UUID userId = UUID.randomUUID();

        when(passwordEncoder.encode(PASSWORD)).thenReturn(encoded);
        when(jwtService.generateToken(any(User.class))).thenReturn(token);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return null;
        }).when(userService).createUser(any(User.class));

        AuthenticationResponse response = authenticationService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(userCaptor.capture());
        User createdUser = userCaptor.getValue();
        assertEquals(FIRSTNAME, createdUser.getFirstname());
        assertEquals(LASTNAME, createdUser.getLastname());
        assertEquals(EMAIL, createdUser.getEmail());
        assertEquals(encoded, createdUser.getPassword());
        assertEquals(Role.ROLE_USER, createdUser.getRole());
        verify(jwtService).generateToken(createdUser);
        assertEquals(token, response.getToken());
        assertEquals(userId, response.getUserId());
    }

    @Test
    void authenticate_authenticatesAndReturnsToken() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();
        UUID userId = UUID.randomUUID();
        User user = new User(FIRSTNAME, LASTNAME, BIRTH_DATE, EMAIL, "hashed", Role.ROLE_USER);
        user.setId(userId);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        AuthenticationResponse response = authenticationService.authenticate(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken authToken = authCaptor.getValue();
        assertEquals(EMAIL, authToken.getPrincipal());
        assertEquals(PASSWORD, authToken.getCredentials());
        verify(jwtService).generateToken(user);
        assertEquals(TOKEN, response.getToken());
        assertEquals(userId, response.getUserId());
    }
}
