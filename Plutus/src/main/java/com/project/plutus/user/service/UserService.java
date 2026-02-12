package com.project.plutus.user.service;

import com.project.plutus.user.model.User;
import com.project.plutus.user.model.UserDTO;

import java.util.UUID;

public interface UserService {
    UserDTO getUserById(UUID userId);

    User getUserByEmail(String email);

    void createUser(User user);

    void updateUser(UUID userId, User updatedUser);

    void verifyUser(String userId);
}
