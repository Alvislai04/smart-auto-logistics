package com.fyp.autologistics.config;

import com.fyp.autologistics.service.CustomUserDetailsService;
import com.fyp.autologistics.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTests {

    private final JwtService jwtService = mock(JwtService.class);
    private final CustomUserDetailsService customUserDetailsService =
            mock(CustomUserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtService,
            customUserDetailsService
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void invalidBearerTokenReturnsUnauthorizedJsonAndStopsFilterChain()
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/shipments");
        request.setServletPath("/api/shipments");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired.jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("old-user", null));
        when(jwtService.extractEmail("expired.jwt")).thenThrow(new JwtException("JWT expired"));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString())
                .contains("Your session has expired")
                .contains("/api/shipments");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(filterChain, never()).doFilter(any(), any());
        verifyNoInteractions(customUserDetailsService);
    }

    @Test
    void authEndpointsSkipJwtParsing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setServletPath("/api/auth/login");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired.jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, customUserDetailsService);
    }
}
