package com.saif.contactmanagement.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import com.saif.contactmanagement.service.impl.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // 256-bit key base64 encoded
    @SuppressWarnings("java:S6418")
    private static final String TEST_SECRET = "c3VwZXJzZWNyZXRrZXlzdXBlcnNlY3JldGtleXN1cGVyc2VjcmV0a2V5";

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600L); // 1 hour in seconds

        userDetails = new User(
                "john.doe@example.com",
                "password",
                Collections.emptyList()
        );
    }

    @Test
    void shouldGenerateAndExtractUsernameCorrectly() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("john.doe@example.com", username);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void shouldFailTokenValidationWhenUsernameDiffers() {
        String token = jwtService.generateToken(userDetails);
        UserDetails differentUser = new User(
                "other@example.com",
                "password",
                Collections.emptyList()
        );
        boolean isValid = jwtService.isTokenValid(token, differentUser);
        assertFalse(isValid);
    }

    @Test
    void shouldFailTokenValidationWhenCredentialVersionDiffers() {
        com.saif.contactmanagement.entity.User userEntity = com.saif.contactmanagement.entity.User.builder()
                .email("john.doe@example.com")
                .password("password")
                .credentialVersion(1)
                .build();
        CustomUserDetails customUserDetails1 = new CustomUserDetails(userEntity);

        String token = jwtService.generateToken(customUserDetails1);

        // Increment user's credential version
        userEntity.setCredentialVersion(2);
        CustomUserDetails customUserDetails2 = new CustomUserDetails(userEntity);

        boolean isValid = jwtService.isTokenValid(token, customUserDetails2);
        assertFalse(isValid);
    }

    @Test
    void shouldThrowSignatureExceptionWhenTokenTampered() {
        String token = jwtService.generateToken(userDetails);
        String tamperedToken = token + "modified";

        Exception exception = assertThrows(SignatureException.class, () -> jwtService.extractUsername(tamperedToken));
        assertNotNull(exception);
    }

    @Test
    void shouldThrowMalformedJwtExceptionWhenTokenMalformed() {
        String malformedToken = "invalidTokenHeader.payload.signature";

        Exception exception = assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(malformedToken));
        assertNotNull(exception);
    }

    @Test
    void shouldThrowExpiredJwtExceptionWhenTokenExpired() {
        // Set short expiration (e.g. -1s)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1L);
        String token = jwtService.generateToken(userDetails);

        Exception exception = assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
        assertNotNull(exception);
    }

    @Test
    void shouldExposeJwtExpirationTime() {
        long expiration = jwtService.getJwtExpiration();
        assertEquals(3600L, expiration);
    }
}
