
package com.finsight.controller;

import com.finsight.exceptions.GlobalExceptionHandler;
import com.finsight.exceptions.InvalidCredentialsException;
import com.finsight.model.User;
import com.finsight.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void signupShouldReturnCreatedWhenSuccessful() throws Exception {

        User user = new User();
        user.setEmail("aruk@example.com");

        when(authService.signup(
                "Aruk Ayuk",
                "aruk@example.com",
                "0712345678",
                "password123"
        )).thenReturn(user);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Aruk Ayuk",
                                    "email": "aruk@example.com",
                                    "phoneNumber": "0712345678",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string(
                        "Account created successfully for aruk@example.com"
                ));

        verify(authService).signup(
                "Aruk Ayuk",
                "aruk@example.com",
                "0712345678",
                "password123"
        );
    }

    @Test
    void signupShouldReturnBadRequestWhenEmailAlreadyExists()
            throws Exception {

        when(authService.signup(
                anyString(), anyString(), anyString(), anyString()
        )).thenThrow(
                new IllegalArgumentException("Email is already registered")
        );

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Aruk Ayuk",
                                    "email": "aruk@example.com",
                                    "phoneNumber": "0712345678",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already registered"));
    }

    @Test
    void loginShouldReturnTokenWhenSuccessful() throws Exception {

        when(authService.login("aruk@example.com", "password123"))
                .thenReturn("mock-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "aruk@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));

        verify(authService).login("aruk@example.com", "password123");
    }

    @Test
    void loginShouldReturnUnauthorizedWhenCredentialsAreInvalid()
            throws Exception {

        when(authService.login("aruk@example.com", "wrongPassword"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "aruk@example.com",
                                    "password": "wrongPassword"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }
}
