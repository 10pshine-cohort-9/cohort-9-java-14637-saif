package com.saif.contactmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saif.contactmanagement.dto.request.ChangePasswordRequest;
import com.saif.contactmanagement.dto.request.UserProfileRequest;
import com.saif.contactmanagement.dto.response.UserResponse;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.exception.GlobalExceptionHandler;
import com.saif.contactmanagement.service.UserService;
import com.saif.contactmanagement.service.impl.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User currentUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        currentUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .build();

        userResponse = new UserResponse(
                1L,
                "John",
                "Doe",
                "1234567890",
                "test@example.com"
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(User user) {
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(user);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // --- Get Profile Tests ---

    @Test
    void shouldGetProfileSuccessfully() throws Exception {
        mockSecurityContext(currentUser);
        when(userService.getProfile(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getProfile(1L);
    }

    // --- Update Profile Tests ---

    @Test
    void shouldUpdateProfileSuccessfully() throws Exception {
        mockSecurityContext(currentUser);
        UserProfileRequest request = new UserProfileRequest("Jane", "Smith", "9876543210");
        UserResponse updatedResponse = new UserResponse(1L, "Jane", "Smith", "9876543210", "test@example.com");

        when(userService.updateProfile(eq(1L), any(UserProfileRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"));

        verify(userService).updateProfile(eq(1L), any(UserProfileRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateProfileValidationFails() throws Exception {
        UserProfileRequest invalidRequest = new UserProfileRequest("", "", "invalid-phone");

        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());

        verify(userService, never()).updateProfile(anyLong(), any(UserProfileRequest.class));
    }

    // --- Change Password Tests ---

    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        mockSecurityContext(currentUser);
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123", "newPassword123");

        doNothing().when(userService).changePassword(1L, "oldPassword123", "newPassword123");

        mockMvc.perform(post("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).changePassword(1L, "oldPassword123", "newPassword123");
    }

    @Test
    void shouldReturnBadRequestWhenChangePasswordValidationFails() throws Exception {
        ChangePasswordRequest invalidRequest = new ChangePasswordRequest("", "short");

        mockMvc.perform(post("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).changePassword(anyLong(), anyString(), anyString());
    }

    @Test
    void shouldThrowBadCredentialsWhenAuthenticationIsNull() throws Exception {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldThrowBadCredentialsWhenPrincipalIsInvalid() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("invalid-principal");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }
}
