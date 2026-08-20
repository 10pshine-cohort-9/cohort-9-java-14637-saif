package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.dto.request.LoginRequest;
import com.saif.contactmanagement.dto.request.UserRegistrationRequest;
import com.saif.contactmanagement.dto.response.LoginResponse;
import com.saif.contactmanagement.dto.response.UserResponse;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.exception.EmailAlreadyExistsException;
import com.saif.contactmanagement.repository.UserRepository;
import com.saif.contactmanagement.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("ResultOfMethodCallIgnored")
class UserServiceImplTest {

    private static final String USER_EMAIL = "john.doe@example.com";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String NEW_PASSWORD = "newPassword";

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRegistrationRequest registrationRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email(USER_EMAIL)
                .phoneNumber("1234567890")
                .password(ENCODED_PASSWORD)
                .build();

        registrationRequest = new UserRegistrationRequest(
                "John",
                "Doe",
                "1234567890",
                USER_EMAIL,
                "password"
        );

        loginRequest = new LoginRequest(
                USER_EMAIL,
                "password"
        );
    }

    // --- Registration Tests ---

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.register(registrationRequest);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(user.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(user.getEmail(), response.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(EmailAlreadyExistsException.class, () -> userService.register(registrationRequest));
        assertNotNull(exception);

        verify(userRepository, never()).save(any(User.class));
    }

    // --- Login Tests ---

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(userDetails)).thenReturn("dummyJwtToken");
        when(jwtService.getJwtExpiration()).thenReturn(3600L);

        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("dummyJwtToken", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(user.getEmail(), response.getUser().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void shouldRejectLoginWhenPasswordIsIncorrect() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        Exception exception = assertThrows(BadCredentialsException.class, () -> userService.login(loginRequest));
        assertNotNull(exception);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void shouldRejectLoginWhenUserNotFound() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> userService.login(loginRequest));
        assertNotNull(exception);
    }

    // --- Change Password Tests ---

    @Test
    void shouldChangePasswordSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("newEncodedPassword");

        userService.changePassword(1L, OLD_PASSWORD, NEW_PASSWORD);

        assertEquals("newEncodedPassword", user.getPassword());
        assertEquals(2, user.getCredentialVersion());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectChangePasswordWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> userService.changePassword(1L, OLD_PASSWORD, NEW_PASSWORD));
        assertNotNull(exception);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectChangePasswordWhenOldPasswordIncorrect() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", ENCODED_PASSWORD)).thenReturn(false);

        Exception exception = assertThrows(BadCredentialsException.class, () -> userService.changePassword(1L, "wrongOldPassword", NEW_PASSWORD));
        assertNotNull(exception);
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Profile Tests ---

    @Test
    void shouldGetProfileSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile(1L);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
    }

    @Test
    void shouldRejectGetProfileWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getProfile(999L));
    }

    @Test
    void shouldUpdateProfileSuccessfully() {
        com.saif.contactmanagement.dto.request.UserProfileRequest request = 
                new com.saif.contactmanagement.dto.request.UserProfileRequest("Jane", "Smith", "9876543210");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateProfile(1L, request);

        assertNotNull(response);
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("9876543210", user.getPhoneNumber());
    }

    @Test
    void shouldRejectUpdateProfileWhenUserNotFound() {
        com.saif.contactmanagement.dto.request.UserProfileRequest request = 
                new com.saif.contactmanagement.dto.request.UserProfileRequest("Jane", "Smith", "9876543210");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.updateProfile(999L, request));
        verify(userRepository, never()).save(any(User.class));
    }
}
