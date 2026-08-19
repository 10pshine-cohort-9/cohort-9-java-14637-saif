package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    private User user;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .credentialVersion(5)
                .build();

        customUserDetails = new CustomUserDetails(user);
    }

    @Test
    void shouldReturnCorrectAuthorities() {
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void shouldReturnCorrectPassword() {
        assertEquals("password123", customUserDetails.getPassword());
    }

    @Test
    void shouldReturnCorrectUsername() {
        assertEquals("test@example.com", customUserDetails.getUsername());
    }

    @Test
    void shouldReturnTrueForAccountStatusMethods() {
        assertTrue(customUserDetails.isAccountNonExpired());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());
        assertTrue(customUserDetails.isEnabled());
    }

    @Test
    void shouldReturnCorrectCredentialVersion() {
        assertEquals(5, customUserDetails.getCredentialVersion());
    }

    @Test
    void shouldReturnCorrectUser() {
        assertEquals(user, customUserDetails.getUser());
    }
}
