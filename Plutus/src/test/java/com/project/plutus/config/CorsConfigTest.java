package com.project.plutus.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    @Test
    void corsConfigurer_registersExpectedMappings() throws IllegalAccessException {
        CorsConfig config = new CorsConfig();
        WebMvcConfigurer configurer = config.corsConfigure();
        CorsRegistry registry = new CorsRegistry();

        configurer.addCorsMappings(registry);

        Object registrations = ReflectionTestUtils.getField(registry, "registrations");
        assertNotNull(registrations);
        assertTrue(registrations instanceof List<?>);
        List<?> list = (List<?>) registrations;
        assertEquals(1, list.size());

        Object registration = list.get(0);
        CorsConfiguration corsConfiguration = extractCorsConfiguration(registration);
        assertNotNull(corsConfiguration);

        assertTrue(corsConfiguration.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(corsConfiguration.getAllowedOrigins().contains("http://localhost:8000"));
        assertTrue(corsConfiguration.getAllowedOrigins().contains("https://plutus-finance-app.com"));
        assertTrue(corsConfiguration.getAllowedMethods().containsAll(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS")));
        assertTrue(corsConfiguration.getAllowedHeaders().contains("*"));
        assertEquals(Boolean.TRUE, corsConfiguration.getAllowCredentials());

        String pathPattern = extractPathPattern(registration);
        assertEquals("/**", pathPattern);
    }

    private static CorsConfiguration extractCorsConfiguration(Object registration) throws IllegalAccessException {
        for (Field field : registration.getClass().getDeclaredFields()) {
            if (CorsConfiguration.class.equals(field.getType())) {
                field.setAccessible(true);
                return (CorsConfiguration) field.get(registration);
            }
        }
        return null;
    }

    private static String extractPathPattern(Object registration) throws IllegalAccessException {
        for (Field field : registration.getClass().getDeclaredFields()) {
            if (String.class.equals(field.getType())) {
                field.setAccessible(true);
                Object value = field.get(registration);
                if ("/**".equals(value)) {
                    return (String) value;
                }
            }
        }
        return null;
    }
}
