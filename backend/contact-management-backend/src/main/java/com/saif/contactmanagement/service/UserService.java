package com.saif.contactmanagement.service;

import com.saif.contactmanagement.dto.request.LoginRequest;
import com.saif.contactmanagement.dto.request.UserRegistrationRequest;
import com.saif.contactmanagement.dto.response.LoginResponse;
import com.saif.contactmanagement.dto.response.UserResponse;

public interface UserService {

    UserResponse register(UserRegistrationRequest request);

    LoginResponse login(LoginRequest request);
    void changePassword(Long userId,
                        String oldPassword,
                        String newPassword);

    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, com.saif.contactmanagement.dto.request.UserProfileRequest request);
}