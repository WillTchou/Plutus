package com.project.plutus.config;

import com.project.plutus.user.model.Role;
import com.project.plutus.user.model.User;
import com.project.plutus.user.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    private static final String EMAIL = "jane@plutus.com";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationConfig applicationConfig;

    @Test
    void userDetailsService_returnsUser() {
        User user = getUser("hashed");
        when(userRepository.findUserByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserDetailsService service = applicationConfig.userDetailsService();
        UserDetails result = service.loadUserByUsername(EMAIL);

        assertEquals(user, result);
        verify(userRepository).findUserByEmail(EMAIL);
    }

    @Test
    void userDetailsService_throws_whenMissing() {
        when(userRepository.findUserByEmail("missing@plutus.com")).thenReturn(Optional.empty());

        UserDetailsService service = applicationConfig.userDetailsService();

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@plutus.com"));
        verify(userRepository).findUserByEmail("missing@plutus.com");
    }

    @Test
    void authenticationProvider_usesUserDetailsServiceAndPasswordEncoder() {
        String rawPassword = "password";
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        User user = getUser(encoder.encode(rawPassword));
        when(userRepository.findUserByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthenticationProvider provider = applicationConfig.authenticationProvider();

        assertInstanceOf(DaoAuthenticationProvider.class, provider);
        assertNotNull(provider.authenticate(new UsernamePasswordAuthenticationToken(EMAIL, rawPassword)));
        verify(userRepository).findUserByEmail(EMAIL);
    }

    @Test
    void authenticationManager_returnsFromConfiguration() throws Exception {
        AuthenticationManager expected = authentication -> authentication;
        AuthenticationConfiguration configuration = org.mockito.Mockito.mock(AuthenticationConfiguration.class);
        when(configuration.getAuthenticationManager()).thenReturn(expected);

        AuthenticationManager actual = applicationConfig.authenticationManager(configuration);

        assertEquals(expected, actual);
    }

    @Test
    void passwordEncoder_isBcrypt() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();

        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    private static @NonNull User getUser(String encoder) {
        return new User("Jane", "Doe", "1990-01-01", EMAIL, encoder, Role.ROLE_USER);
    }
}
