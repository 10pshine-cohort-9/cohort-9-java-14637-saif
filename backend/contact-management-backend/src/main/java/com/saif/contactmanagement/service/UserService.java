package com.saif.contactmanagement.service;

import com.saif.contactmanagement.entity.User;

public interface UserService {

    User register(User user);

    User login(String email, String password);

    void changePassword(Long userId, String oldPassword, String newPassword);
}