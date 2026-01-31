package com.project.plutus.config;

import com.project.plutus.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwtToken;
        final String username;

        if (doesAuthHeaderStartWithInvalidAuthorizationType(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }
        jwtToken = authHeader.substring(7);
        username = jwtService.extractUsername(jwtToken);
        if (hasNotUsernameAuthentication(username)) {
            final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtService.isValidToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                setAuthenticationTokenDetails(authenticationToken, request);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean doesAuthHeaderStartWithInvalidAuthorizationType(final String authHeader) {
        return authHeader == null || !authHeader.startsWith("Bearer ");
    }

    private boolean hasNotUsernameAuthentication(final String username) {
        return username != null && SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private void setAuthenticationTokenDetails(final UsernamePasswordAuthenticationToken authenticationToken,
                                               final HttpServletRequest request) {
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    }
}
