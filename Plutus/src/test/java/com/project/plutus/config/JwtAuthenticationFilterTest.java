package com.project.plutus.config;

import com.project.plutus.auth.service.JwtService;
import com.project.plutus.user.model.Role;
import com.project.plutus.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String AUTHORIZATION = "Authorization";
    private static final String EMAIL = "jane@plutus.com";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_skips_whenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    void doFilter_skips_whenAuthorizationNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, "Basic abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    void doFilter_setsAuthentication_whenTokenValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails user = new User("Jane", "Doe", "1990-01-01", EMAIL, "pw", Role.ROLE_USER);
        when(jwtService.extractUsername("token")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(user);
        when(jwtService.isValidToken("token", user)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).isAuthenticated());
        assertEquals(EMAIL, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilter_doesNotAuthenticate_whenTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails user = new User("Jane", "Doe", "1990-01-01", EMAIL, "pw", Role.ROLE_USER);
        when(jwtService.extractUsername("token")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(user);
        when(jwtService.isValidToken("token", user)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
