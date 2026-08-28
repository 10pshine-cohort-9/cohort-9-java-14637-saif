package com.saif.contactmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saif.contactmanagement.dto.request.LoginRequest;
import com.saif.contactmanagement.dto.request.UserRegistrationRequest;
import com.saif.contactmanagement.dto.response.LoginResponse;
import com.saif.contactmanagement.dto.response.UserResponse;
import com.saif.contactmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.saif.contactmanagement.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John", "Doe", "1234567890", "john.doe@example.com", "password"
        );
        UserResponse response = new UserResponse(
                1L, "John", "Doe", "1234567890", "john.doe@example.com"
        );

        when(userService.register(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).register(any(UserRegistrationRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenRegistrationValidationFails() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "", "", "", "invalid-email", ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());

        verify(userService, never()).register(any(UserRegistrationRequest.class));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@example.com", "password");
        UserResponse userResponse = new UserResponse(1L, "John", "Doe", "1234567890", "john.doe@example.com");
        LoginResponse response = new LoginResponse("dummyToken", "Bearer", 3600L, userResponse);

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.expiresIn").value(3600L))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                .andExpect(cookie().exists("ACCESS_TOKEN"))
                .andExpect(cookie().value("ACCESS_TOKEN", "dummyToken"))
                .andExpect(cookie().httpOnly("ACCESS_TOKEN", true))
                .andExpect(cookie().maxAge("ACCESS_TOKEN", 3600));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    void shouldClearAccessTokenCookieOnLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("ACCESS_TOKEN", 0));
    }

    @Test
    void shouldReturnBadRequestWhenLoginValidationFails() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());

        verify(userService, never()).login(any(LoginRequest.class));
    }
}
