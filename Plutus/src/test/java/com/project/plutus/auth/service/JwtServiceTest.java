package com.project.plutus.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=");
    }

    @Test
    void generateToken_andValidate() {
        UserDetails user = new User("user@plutus.com", "pass", List.of());

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("user@plutus.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isValidToken(token, user));
    }

    @Test
    void isValidToken_falseForDifferentUser() {
        UserDetails user = new User("user@plutus.com", "pass", List.of());
        UserDetails other = new User("other@plutus.com", "pass", List.of());

        String token = jwtService.generateToken(user);

        assertFalse(jwtService.isValidToken(token, other));
    }
}
