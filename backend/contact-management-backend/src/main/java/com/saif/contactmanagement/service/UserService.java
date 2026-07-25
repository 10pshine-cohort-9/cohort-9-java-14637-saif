package com.saif.contactmanagement.service;

import com.saif.contactmanagement.dto.request.UserRegistrationRequest;
import com.saif.contactmanagement.dto.response.UserResponse;
import com.saif.contactmanagement.entity.User;

public interface UserService {

    UserResponse register(UserRegistrationRequest request);

    User login(String email, String password);

    void changePassword(Long userId,
                        String oldPassword,
                        String newPassword);

}