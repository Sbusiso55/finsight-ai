
package com.finsight.config;

import com.finsight.security.CustomUserDetailsService;

import com.finsight.security.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    private JwtService jwtService;
    private CustomUserDetailsService userDetailsService;
    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(CustomUserDetailsService.class);

        authenticationFilter = new AuthenticationFilter(
                jwtService,
                userDetailsService
        );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenShouldAuthenticateUser()
            throws ServletException, IOException {

        String email = "test@example.com";
        String token = "valid-token";

        UserDetails userDetails = mock(UserDetails.class);

        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.getAuthorities())
                .thenReturn(java.util.Collections.emptyList());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/test");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
        assertEquals(200, response.getStatus());
        verify(userDetailsService).loadUserByUsername(email);
    }

    @Test
    void missingAuthorizationHeaderShouldAllowRequestToContinue()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/test");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
        assertEquals(200, response.getStatus());
    }

    @Test
    void nonBearerAuthorizationHeaderShouldAllowRequestToContinue()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/test");
        request.addHeader("Authorization", "Basic some-credentials");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
        assertEquals(200, response.getStatus());
    }

    @Test
    void invalidTokenShouldReturnUnauthorized()
            throws ServletException, IOException {

        String token = "invalid-token";

        when(jwtService.extractEmail(token))
                .thenThrow(new MalformedJwtException("Invalid token"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/test");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authenticationFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void disabledUserShouldReturnUnauthorized()
            throws ServletException, IOException {

        String email = "disabled@example.com";
        String token = "valid-token";

        UserDetails userDetails = mock(UserDetails.class);

        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/test");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        authenticationFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Account is disabled"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void authenticationEndpointsShouldBypassFilter()
            throws ServletException, IOException {

        String[] paths = {"/auth/login", "/auth/signup"};

        for (String path : paths) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setServletPath(path);
            request.addHeader("Authorization", "Bearer invalid-token");

            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            authenticationFilter.doFilter(request, response, filterChain);

            assertEquals(200, response.getStatus());
        }

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }
}
