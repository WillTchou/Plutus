package com.project.plutus.user.service;

import com.project.plutus.exceptions.EmailAlreadyExistsException;
import com.project.plutus.exceptions.UserDoesNotExistException;
import com.project.plutus.user.mapper.UserMapper;
import com.project.plutus.user.model.KycState;
import com.project.plutus.user.model.User;
import com.project.plutus.user.model.UserDTO;
import com.project.plutus.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(final UserRepository userRepository, final UserMapper userMapper,
                           final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getUserById(final UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserDTO)
                .orElseThrow(UserDoesNotExistException::new);
    }

    @Override
    public User getUserByEmail(final String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public void createUser(final User user) {
        final String userEmail = user.getEmail();
        final Optional<User> optionalUser = userRepository.findUserByEmail(userEmail);
        if (optionalUser.isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        userRepository.save(user);
    }

    @Override
    public void updateUser(final UUID userId, final User updatedUser) {
        userRepository.findById(userId)
                .ifPresentOrElse(userToUpdate -> {
                    userMapper.mapUpdateUser(updatedUser, userToUpdate, passwordEncoder);
                    userRepository.save(userToUpdate);
                }, UserDoesNotExistException::new);
    }

    @Override
    public void verifyUser(final String userId) {
        userRepository.findById(UUID.fromString(userId))
                .ifPresentOrElse(user -> {
                    user.setKycState(KycState.VERIFIED);
                    userRepository.save(user);
                }, UserDoesNotExistException::new);
    }
}
