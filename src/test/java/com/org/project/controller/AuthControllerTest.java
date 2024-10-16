package com.org.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.project.exception.UnauthorizedException;
import com.org.project.model.User;
import com.org.project.model.auth.LoginRequestDTO;
import com.org.project.model.auth.RegisterRequestDTO;
import com.org.project.service.AuthService;
import com.org.project.service.UserService;
import com.org.project.component.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("testId");
        testUser.setEmail("test@example.com");
        testUser.setProvider(User.Provider.LOCAL);
        testUser.setAuthVersion(1);
    }

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.id").value("testId"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(cookie().exists("JWT_Access_Token"))
                .andExpect(cookie().exists("JWT_Refresh_Token"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Login failed: Invalid credentials"));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO("Test", "test@example.com", "testtest", User.Provider.LOCAL);

        when(userService.registerUser(any(RegisterRequestDTO.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User successfully registered"))
                .andExpect(jsonPath("$.id").value("testId"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(cookie().exists("JWT_Access_Token"))
                .andExpect(cookie().exists("JWT_Refresh_Token"));
    }

    @Test
    public void testRefreshSuccess() throws Exception {
        when(authUtil.getRefreshCookie(any())).thenReturn(new jakarta.servlet.http.Cookie("JWT_Refresh_Token", "valid_token"));
        when(authUtil.getUserIdFromToken(any())).thenReturn("testId");
        when(userService.getUserFromId("testId")).thenReturn(testUser);
        when(authUtil.isTokenValid(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new  jakarta.servlet.http.Cookie("JWT_Refresh_Token", "valid_token")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("User refresh successful"))
                .andExpect(jsonPath("$.id").value("testId"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(cookie().exists("JWT_Access_Token"))
                .andExpect(cookie().exists("JWT_Refresh_Token"));
    }

    @Test
    public void testRefreshFailureNoToken() throws Exception {
        when(authUtil.getRefreshCookie(any())).thenReturn(null);

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token not found"));
    }

    @Test
    public void testRefreshFailureInvalidToken() throws Exception {
        when(authUtil.getRefreshCookie(any())).thenReturn(new  jakarta.servlet.http.Cookie("JWT_Refresh_Token", "invalid_token"));
        when(authUtil.getUserIdFromToken(any())).thenReturn("testId");
        when(userService.getUserFromId("testId")).thenReturn(testUser);
        when(authUtil.isTokenValid(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("JWT_Refresh_Token", "invalid_token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token expired or invalid"));
    }
}