package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    private static final String USER_EMAIL = "john.doe@example.com";
    private static final String NONEXISTENT_EMAIL = "nonexistent@example.com";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email(USER_EMAIL)
                .phoneNumber("1234567890")
                .password("encodedPassword")
                .build();
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(USER_EMAIL);

        assertNotNull(userDetails);
        assertEquals(USER_EMAIL, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("USER")));

        verify(userRepository).findByEmail(USER_EMAIL);
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail(NONEXISTENT_EMAIL)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(NONEXISTENT_EMAIL));
        assertNotNull(exception);

        verify(userRepository).findByEmail(NONEXISTENT_EMAIL);
    }
}
