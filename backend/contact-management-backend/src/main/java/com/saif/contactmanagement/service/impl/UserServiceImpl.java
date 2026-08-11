package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.dto.request.LoginRequest;
import com.saif.contactmanagement.dto.request.UserRegistrationRequest;
import com.saif.contactmanagement.dto.response.LoginResponse;
import com.saif.contactmanagement.dto.response.UserResponse;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.exception.EmailAlreadyExistsException;
import com.saif.contactmanagement.repository.UserRepository;
import com.saif.contactmanagement.service.UserService;
import com.saif.contactmanagement.util.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Constructor Injection
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
                .credentialVersion(1)
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        String token = jwtService.generateToken(userDetails);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getEmail()
        );

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getJwtExpiration(),
                userResponse
        );
    }

    /**
     * Change Password
     */
    @Override
    public void changePassword(Long userId,
                               String oldPassword,
                               String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialVersion(user.getCredentialVersion() + 1);
        userRepository.save(user);
    }
}