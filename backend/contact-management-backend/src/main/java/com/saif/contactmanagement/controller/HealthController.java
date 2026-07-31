package com.saif.contactmanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Contact Management API is running";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is healthy";
    }
}