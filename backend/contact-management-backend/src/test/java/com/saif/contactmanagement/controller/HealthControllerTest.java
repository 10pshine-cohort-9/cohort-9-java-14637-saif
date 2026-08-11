package com.saif.contactmanagement.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    void shouldReturnHomeMessage() {
        assertEquals("Contact Management API is running", healthController.home());
    }

    @Test
    void shouldReturnHealthMessage() {
        assertEquals("Application is healthy", healthController.health());
    }
}
