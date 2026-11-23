
package com.finsight.security;

import com.finsight.exceptions.InvalidCredentialsException;
import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFullName("Aruk Ayuk");
        user.setEmail("aruk@example.com");
        user.setPhoneNumber("0712345678");
        user.setPasswordHash("encodedPassword");
    }

    @Test
    void signupShouldSaveUserWithEncodedPassword() {
        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = authService.signup(
                "Aruk Ayuk",
                "aruk@example.com",
                "0712345678",
                "password123"
        );

        assertNotNull(savedUser);
        assertEquals("aruk@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPasswordHash());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signupShouldRejectExistingEmail() {
        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                authService.signup(
                        "Aruk Ayuk",
                        "aruk@example.com",
                        "0712345678",
                        "password123"
                ));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken(user.getEmail()))
                .thenReturn("mock-jwt-token");

        String token = authService.login(
                "aruk@example.com",
                "password123"
        );

        assertEquals("mock-jwt-token", token);
        verify(jwtService).generateToken(user.getEmail());
    }

    @Test
    void loginShouldRejectUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login("unknown@example.com", "password123"));

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void loginShouldRejectIncorrectPassword() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login(user.getEmail(), "wrongPassword"));

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void loginShouldRejectDisabledUser() {
        user.setEnabled(false);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login(user.getEmail(), "password123"));

        verify(jwtService, never()).generateToken(anyString());
    }
}
