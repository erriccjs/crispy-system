package com.myproject.crispysystem.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.crispysystem.users.payload.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthTokenFilterTest {
    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext(); // Clear SecurityContext before each test
    }

    @Test
    void testLoginEndpointBypassesFilter() throws Exception {
        request.setRequestURI("/api/login");

        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Verify the filter chain continues
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testTokenMissingReturnsUnauthorized() throws Exception {
        request.setRequestURI("/api/protected");

        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Verify response
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        // Verify the filter chain does not continue
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testInvalidTokenReturnsForbidden() throws Exception {
        request.setRequestURI("/api/protected");
        request.addHeader("Authorization", "Bearer invalid.token");

        when(jwtUtil.validateToken("invalid.token")).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Verify response
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());

        // Verify the filter chain does not continue
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testValidTokenSetsAuthentication() throws Exception {
        request.setRequestURI("/api/protected");
        request.addHeader("Authorization", "Bearer valid.token");

        when(jwtUtil.validateToken("valid.token")).thenReturn(true);
        when(jwtUtil.getSubjectFromToken("valid.token")).thenReturn("testUserId");

        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Verify SecurityContext is updated
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testUserId", SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // Verify the filter chain continues
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
