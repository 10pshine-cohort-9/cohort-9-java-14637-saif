package com.saif.contactmanagement.controller;

import com.saif.contactmanagement.dto.request.ChangePasswordRequest;
import com.saif.contactmanagement.dto.request.UserProfileRequest;
import com.saif.contactmanagement.dto.response.UserResponse;
import com.saif.contactmanagement.service.UserService;
import com.saif.contactmanagement.service.impl.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
        }
        throw new BadCredentialsException("User not authenticated");
    }

    @GetMapping("/profile")
    public UserResponse getProfile() {
        Long userId = getCurrentUserId();
        log.info("Fetching profile details for user ID: {}", userId);
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public UserResponse updateProfile(@Valid @RequestBody UserProfileRequest request) {
        Long userId = getCurrentUserId();
        log.info("Updating profile details for user ID: {}", userId);
        return userService.updateProfile(userId, request);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        log.info("Changing password for user ID: {}", userId);
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
    }
}
