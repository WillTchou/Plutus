package com.project.plutus.user.controller;

import com.project.plutus.user.model.UserDTO;
import com.project.plutus.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") final UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping(path = "/{id}")
    public ResponseEntity<Void> verifyUser(@PathVariable("id") final String userId) {
        userService.verifyUser(userId);
        return ResponseEntity.noContent().build();
    }
}
