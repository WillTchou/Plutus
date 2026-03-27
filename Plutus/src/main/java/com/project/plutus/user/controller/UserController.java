package com.project.plutus.user.controller;

import com.project.plutus.user.model.UserDTO;
import com.project.plutus.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getAuthedUser(final Authentication authentication) {
        final String userEmail = authentication.getName();
        return ResponseEntity.ok(userService.getAuthedUser(userEmail));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyUser(final Authentication authentication) {
        final String userEmail = authentication.getName();
        userService.verifyUser(userEmail);
        return ResponseEntity.noContent().build();
    }
}
