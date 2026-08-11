package com.saif.contactmanagement.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityConfigTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DaoAuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void securityBeansShouldExist() {
        assertNotNull(passwordEncoder);
        assertNotNull(authenticationManager);
        assertNotNull(authenticationProvider);
    }

    @Test
    void publicEndpointsShouldBeAccessibleWithoutAuthentication() throws Exception {
        // /api/auth/login and /api/auth/register should be permitAll
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isBadRequest()); // validation error because empty body, but NOT 401 Unauthorized!
    }

    @Test
    void protectedEndpointsShouldRequireAuthentication() throws Exception {
        // A protected endpoint (e.g. /api/health) should return 401 Unauthorized
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isUnauthorized());
    }
}
