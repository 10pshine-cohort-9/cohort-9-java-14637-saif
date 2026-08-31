package com.saif.contactmanagement.filter;

import com.saif.contactmanagement.service.impl.CustomUserDetailsService;
import com.saif.contactmanagement.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String VALID_TOKEN = "validToken";
    private static final String USER_EMAIL = "john@example.com";

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static Cookie[] accessTokenCookie(String token) {
        return new Cookie[]{new Cookie(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME, token)};
    }

    @Test
    void shouldContinueFilterChainWhenNoCookies() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChainWhenAccessTokenCookieMissing() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("other", "value")});

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreAuthorizationHeaderWithoutAccessTokenCookie() throws ServletException, IOException {
        // The Authorization header is no longer a supported token source; only the HttpOnly cookie is.
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(request, never()).getHeader(AUTH_HEADER);
        verify(jwtService, never()).extractUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChainWhenTokenExtractionFails() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(accessTokenCookie("invalid.token"));
        when(jwtService.extractUsername("invalid.token")).thenThrow(new RuntimeException("Parsing error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldPopulateSecurityContextFromAccessTokenCookie() throws ServletException, IOException {
        String token = VALID_TOKEN;
        String email = USER_EMAIL;
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        when(request.getCookies()).thenReturn(accessTokenCookie(token));
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotPopulateSecurityContextWhenTokenIsInvalid() throws ServletException, IOException {
        String token = "invalidToken";
        String email = USER_EMAIL;
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getCookies()).thenReturn(accessTokenCookie(token));
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenAlreadyAuthenticated() throws ServletException, IOException {
        String token = VALID_TOKEN;
        String email = USER_EMAIL;
        org.springframework.security.core.Authentication existingAuth = mock(org.springframework.security.core.Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getCookies()).thenReturn(accessTokenCookie(token));
        when(jwtService.extractUsername(token)).thenReturn(email);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenUserEmailIsNull() throws ServletException, IOException {
        String token = VALID_TOKEN;

        when(request.getCookies()).thenReturn(accessTokenCookie(token));
        when(jwtService.extractUsername(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleUsernameNotFoundExceptionGracefully() throws ServletException, IOException {
        String token = VALID_TOKEN;
        String email = USER_EMAIL;

        when(request.getCookies()).thenReturn(accessTokenCookie(token));
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("Deleted user"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
