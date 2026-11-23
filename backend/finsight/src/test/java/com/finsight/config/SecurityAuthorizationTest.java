package com.finsight.config;

import com.finsight.security.CustomUserDetailsService;
import com.finsight.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@WebMvcTest(
        controllers = SecurityAuthorizationTest.TestController.class,
        properties = "jwt.secret=test-secret-key-for-jwt-tests-32-bytes!"
)
@EnableWebSecurity
@Import({
        SecurityConfig.class,
        AuthenticationFilter.class,
        JwtService.class,
        SecurityAuthorizationTest.TestConfig.class
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldAllowPublicEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/auth/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Public endpoint"));
    }

    @Test
    void shouldRejectUnauthenticatedRequestToProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUserToAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/test")
                        .with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected endpoint"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    public static class TestController {

        @GetMapping("/auth/test")
        public String publicEndpoint() {
            return "Public endpoint";
        }

        @GetMapping("/api/test")
        public String protectedEndpoint() {
            return "Protected endpoint";
        }
    }

    @Test
    void shouldAllowAccessWithValidJwtToken() throws Exception {

        String email = "test@example.com";

        // Generate a real JWT
        String token = jwtService.generateToken(email);

        // Simulate loading the user from the database
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(
                        User.withUsername(email)
                                .password("password")
                                .authorities("ROLE_USER")
                                .build()
                );

        // Send request with the JWT
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected endpoint"));
    }

    @Test
    void shouldRejectRequestWithInvalidJwtToken() throws Exception {

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestWithExpiredJwtToken() throws Exception {

        String email = "test@example.com";

        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000L);

        String expiredToken = Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now.getTime() - 2000L))
                .expiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(
                        "test-secret-key-for-jwt-tests-32-bytes!"
                                .getBytes(StandardCharsets.UTF_8)
                ))
                .compact();

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectJwtWhenUserAccountIsDisabled() throws Exception {

        String email = "disabled@example.com";
        String token = jwtService.generateToken(email);

        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(
                        User.withUsername(email)
                                .password("password")
                                .authorities("ROLE_USER")
                                .disabled(true)
                                .build()
                );

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Account is disabled"));
    }
}
