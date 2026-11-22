package com.finsight.security;

import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadUserByEmail() {
        String email = "test@example.com";
        String passwordHash = "encoded-password";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setEnabled(true);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        assertEquals(email, userDetails.getUsername());
        assertEquals(passwordHash, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_USER")));

        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldReturnDisabledUserWhenAccountIsDisabled() {
        String email = "disabled@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("encoded-password");
        user.setEnabled(false);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        assertFalse(userDetails.isEnabled());
        assertEquals(email, userDetails.getUsername());
    }
}
