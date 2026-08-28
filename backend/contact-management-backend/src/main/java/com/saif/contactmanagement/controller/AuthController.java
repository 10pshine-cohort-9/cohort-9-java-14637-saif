package com.saif.contactmanagement.controller;

import com.saif.contactmanagement.filter.JwtAuthenticationFilter;
import com.saif.contactmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saif.contactmanagement.dto.request.UserRegistrationRequest;
import com.saif.contactmanagement.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import com.saif.contactmanagement.dto.request.LoginRequest;
import com.saif.contactmanagement.dto.response.LoginResponse;

import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final Environment env;

    @Value("${jwt.cookie.secure:true}")
    private boolean secureCookie;

    public AuthController(UserService userService, Environment env) {
        this.userService = userService;
        this.env = env;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Received registration request");
        return userService.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        log.info("Received login request");

        boolean isLocalDev = env != null && Arrays.asList(env.getActiveProfiles()).contains("local-development");
        if (!isLocalDev) {
            boolean isHttps = httpRequest.isSecure() || "https".equalsIgnoreCase(httpRequest.getHeader("X-Forwarded-Proto"));
            if (!isHttps) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "HTTPS is required for production ingress.");
            }
        }

        LoginResponse loginResponse = userService.login(request);

        boolean secure = isSecureCookieEnforced();
        ResponseCookie accessTokenCookie = ResponseCookie.from(
                        JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME, loginResponse.getAccessToken())
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(loginResponse.getExpiresIn())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        // The token now lives only in the HttpOnly cookie, never in the response body.
        loginResponse.setAccessToken(null);
        return loginResponse;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest httpRequest, HttpServletResponse response) {
        boolean isLocalDev = env != null && Arrays.asList(env.getActiveProfiles()).contains("local-development");
        if (!isLocalDev) {
            boolean isHttps = httpRequest.isSecure() || "https".equalsIgnoreCase(httpRequest.getHeader("X-Forwarded-Proto"));
            if (!isHttps) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "HTTPS is required for production ingress.");
            }
        }

        boolean secure = isSecureCookieEnforced();
        ResponseCookie expiredCookie = ResponseCookie.from(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }

    private boolean isSecureCookieEnforced() {
        if (!secureCookie) {
            if (env != null && Arrays.asList(env.getActiveProfiles()).contains("local-development")) {
                return false;
            }
            return true;
        }
        return true;
    }

}