package com.project.plutus.user.service;

import com.project.plutus.exceptions.EmailAlreadyExistsException;
import com.project.plutus.exceptions.UserDoesNotExistException;
import com.project.plutus.user.mapper.UserMapper;
import com.project.plutus.user.model.KycState;
import com.project.plutus.user.model.Role;
import com.project.plutus.user.model.User;
import com.project.plutus.user.model.UserDTO;
import com.project.plutus.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String EMAIL = "user@plutus.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAuthedUser_returnsDto_whenUserExists() {
        User user = new User("Jane", "Doe", "1990-01-01", EMAIL, "hashed", Role.ROLE_USER);
        UserDTO dto = new UserDTO(UUID.randomUUID(), "Jane", "Doe", EMAIL, KycState.NOT_VERIFIED);

        when(userRepository.findUserByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(dto);

        UserDTO result = userService.getAuthedUser(EMAIL);

        assertEquals(dto, result);
        verify(userRepository).findUserByEmail(EMAIL);
        verify(userMapper).toUserDTO(user);
    }

    @Test
    void getAuthedUser_throws_whenMissing() {
        String email = "missing@plutus.com";
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> userService.getAuthedUser(email));
        verify(userRepository).findUserByEmail(email);
    }

    @Test
    void createUser_saves_whenEmailNotExists() {
        User user = new User("Jane", "Doe", "1990-01-01", EMAIL, "hashed", Role.ROLE_USER);

        when(userRepository.findUserByEmail(EMAIL)).thenReturn(Optional.empty());

        userService.createUser(user);

        verify(userRepository).findUserByEmail(EMAIL);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_throws_whenEmailExists() {
        User user = new User("Jane", "Doe", "1990-01-01", EMAIL, "hashed", Role.ROLE_USER);

        when(userRepository.findUserByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(user));
        verify(userRepository).findUserByEmail(EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_updatesAndSaves_whenUserExists() {
        UUID userId = UUID.randomUUID();
        User existing = new User("Jane", "Doe", "1990-01-01", "a@b.com", "old", Role.ROLE_USER);
        User updated = new User("Janet", "Doe", "1990-01-01", "a@b.com", "new", Role.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

        userService.updateUser(userId, updated);

        verify(userMapper).mapUpdateUser(updated, existing, passwordEncoder);
        verify(userRepository).save(existing);
    }

    @Test
    void verifyUser_setsKycVerified_andSaves() {
        User user = new User("Jane", "Doe", "1990-01-01", EMAIL, "hashed", Role.ROLE_USER);
        user.setKycState(KycState.NOT_VERIFIED);

        when(userRepository.findUserByEmail(EMAIL)).thenReturn(Optional.of(user));

        userService.verifyUser(EMAIL);

        assertEquals(KycState.VERIFIED, user.getKycState());
        verify(userRepository).save(user);
    }
}
