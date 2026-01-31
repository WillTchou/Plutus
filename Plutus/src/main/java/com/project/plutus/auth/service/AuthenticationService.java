package com.project.plutus.auth.service;

import com.project.plutus.auth.model.AuthenticationRequest;
import com.project.plutus.auth.model.AuthenticationResponse;
import com.project.plutus.auth.model.RegisterRequest;
import com.project.plutus.user.model.Role;
import com.project.plutus.user.model.User;
import com.project.plutus.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(final RegisterRequest registerRequest) {
        final User user = buildUserWithRegisterRequest(registerRequest);
        userService.createUser(user);
        final String jwtToken = jwtService.generateToken(user);
        return buildAuthenticationResponse(jwtToken, user.getId());
    }

    public AuthenticationResponse authenticate(final AuthenticationRequest authenticationRequest) {
        String authenticationRequestEmail = authenticationRequest.getEmail();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequestEmail,
                authenticationRequest.getPassword()
        ));
        final User user = userService.getUserByEmail(authenticationRequestEmail);
        final String jwtToken = jwtService.generateToken(user);
        return buildAuthenticationResponse(jwtToken, user.getId());
    }

    private User buildUserWithRegisterRequest(final RegisterRequest registerRequest) {
        final String registerRequestPassword = registerRequest.getPassword();
        return new User(registerRequest.getFirstname(), registerRequest.getLastname(), registerRequest.getBirthdate(),
                registerRequest.getEmail(), passwordEncoder.encode(registerRequestPassword), Role.USER);
    }

    private AuthenticationResponse buildAuthenticationResponse(final String jwtToken, final UUID userId) {
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(userId)
                .build();
    }
}
