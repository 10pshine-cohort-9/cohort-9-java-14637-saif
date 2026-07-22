package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.repository.UserRepository;
import com.saif.contactmanagement.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}