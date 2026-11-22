
package com.finsight.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        AuthenticationFilter authenticationFilter =
                mock(AuthenticationFilter.class);

        securityConfig = new SecurityConfig(authenticationFilter);
    }

    @Test
    void shouldAllowFrontendOrigin() {
        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource();

        CorsConfiguration configuration =
                source.getCorsConfiguration(
                        new MockHttpServletRequest("GET", "/api/test")
                );

        assertNotNull(configuration);

        assertEquals(
                List.of("http://localhost:3000"),
                configuration.getAllowedOrigins()
        );
    }

    @Test
    void shouldAllowConfiguredHttpMethods() {
        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource();

        CorsConfiguration configuration =
                source.getCorsConfiguration(
                        new MockHttpServletRequest("GET", "/api/test")
                );

        assertEquals(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
                configuration.getAllowedMethods()
        );
    }

    @Test
    void shouldAllowConfiguredHeaders() {
        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource();

        CorsConfiguration configuration =
                source.getCorsConfiguration(
                        new MockHttpServletRequest("GET", "/api/test")
                );

        assertEquals(
                List.of("Authorization", "Content-Type"),
                configuration.getAllowedHeaders()
        );
    }
}
