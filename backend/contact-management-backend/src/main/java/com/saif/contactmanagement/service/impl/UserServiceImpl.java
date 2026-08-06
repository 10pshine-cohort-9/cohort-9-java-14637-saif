package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.dto.request.LoginRequest;
import com.saif.contactmanagement.dto.request.UserRegistrationRequest;
import com.saif.contactmanagement.dto.response.LoginResponse;
import com.saif.contactmanagement.dto.response.UserResponse;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.exception.EmailAlreadyExistsException;
import com.saif.contactmanagement.repository.UserRepository;
import com.saif.contactmanagement.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor Injection
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * User Registration
     */
    @Override
    public UserResponse register(UserRegistrationRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email is already registered.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhoneNumber(),
                savedUser.getEmail()
        );
    }

    /**
     * User Login
     */
    @Override
    public LoginResponse login(LoginRequest request) {

        // JWT authentication logic will be implemented next

        return null;
    }

    /**
     * Change Password
     */
    @Override
    public void changePassword(Long userId,
                               String oldPassword,
                               String newPassword) {

        // Will be implemented later

    }
}