package com.saif.contactmanagement.service;

import com.saif.contactmanagement.dto.request.LoginRequest;
import com.saif.contactmanagement.dto.request.UserRegistrationRequest;
import com.saif.contactmanagement.dto.response.LoginResponse;
import com.saif.contactmanagement.dto.response.UserResponse;
import com.saif.contactmanagement.entity.User;

public interface UserService {

    UserResponse register(UserRegistrationRequest request);

    LoginResponse login(LoginRequest request);
    void changePassword(Long userId,
                        String oldPassword,
                        String newPassword);

}