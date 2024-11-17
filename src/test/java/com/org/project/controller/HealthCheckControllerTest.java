package com.org.project.controller;

import com.org.project.security.OrganizationAuthorizationFilter;
import com.org.project.service.AuthService;
import com.org.project.util.AuthUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheckController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganizationAuthorizationFilter organizationAuthorizationFilter;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthUtil authUtil;

    @Test
    public void testHealthCheckEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("API Is Healthy!"));
    }
}
