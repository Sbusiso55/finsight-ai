
package com.finsight.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET =
            "this-is-a-test-only-secret-key-with-more-than-32-bytes";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
    }

    @Test
    void generateTokenShouldContainCorrectEmail() {
        String email = "test@example.com";

        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void extractEmailShouldReturnCorrectEmail() {
        String token = jwtService.generateToken("test@example.com");

        assertEquals("test@example.com", jwtService.extractEmail(token));
    }

    @Test
    void isTokenValidShouldReturnTrueForMatchingEmail() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        assertTrue(jwtService.isTokenValid(token, email));
    }

    @Test
    void isTokenValidShouldReturnFalseForDifferentEmail() {
        String token = jwtService.generateToken("test@example.com");

        assertFalse(jwtService.isTokenValid(token, "another@example.com"));
    }

    @Test
    void isTokenValidShouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid-token", "test@example.com"));
    }
}
