package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.repository.UserRepository;
import com.saif.contactmanagement.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    //constructor injection
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    //register method implementation
    @Override
public User register(User user) {
    return userRepository.save(user);
}
    //login method implementation
    @Override
    public User login(String email, String password) {
        return null;
    }
    //change password method implementation
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {

    }
}