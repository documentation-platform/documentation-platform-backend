package com.org.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.project.TestUtils;
import com.org.project.exception.UnauthorizedException;

import com.org.project.model.User;
import com.org.project.model.auth.AccessToken;
import com.org.project.dto.LoginRequestDTO;
import com.org.project.model.auth.RefreshToken;
import com.org.project.dto.RegisterRequestDTO;
import com.org.project.service.AuthService;
import com.org.project.service.UserService;
import com.org.project.util.AuthUtil;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private Cookie refreshTokenCookie;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("testId");
        testUser.setEmail("test@example.com");
        testUser.setProvider(User.Provider.LOCAL);
        testUser.setAuthVersion(1);

        setupTokenMocks();
    }

    @Nested
    class LoginTests {
        @Test
        void loginSuccessfullyWithValidCredentials() throws Exception {
            LoginRequestDTO loginRequest = new LoginRequestDTO();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("password");
            loginRequest.setProvider(User.Provider.LOCAL);

            when(authService.login(any(LoginRequestDTO.class))).thenReturn(testUser);
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.id").value("testId"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.provider").value("LOCAL"))
                    .andExpect(cookie().exists("JWT_Access_Token"))
                    .andExpect(cookie().exists("JWT_Refresh_Token"));
        }

        @Test
        void loginFailsWithInvalidCredentials() throws Exception {
            LoginRequestDTO loginRequest = new LoginRequestDTO();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("wrongpassword");
            loginRequest.setProvider(User.Provider.LOCAL);

            when(authService.login(any(LoginRequestDTO.class))).thenThrow(new UnauthorizedException("Invalid credentials"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Login failed"));
        }
    }

    @Nested
    class RegisterTests {
        @Test
        void registerSuccessfullyWithValidCredentials() throws Exception {
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("Test", "test@example.com", "testtest", User.Provider.LOCAL);
            when(userService.registerUser(any(RegisterRequestDTO.class))).thenReturn(testUser);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("User successfully registered"))
                    .andExpect(jsonPath("$.id").value("testId"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.provider").value("LOCAL"))
                    .andExpect(cookie().exists("JWT_Access_Token"))
                    .andExpect(cookie().exists("JWT_Refresh_Token"));
        }

        @Test
        void registerFailsWithInvalidCredentials() throws Exception {
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("Test", "test.com", "testtest", User.Provider.LOCAL);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest());

        }
    }

    @Nested
    class RefreshTests {
        @Test
        public void testRefreshSuccess() throws Exception {
            when(authUtil.getTokenFromCookie(any(), eq(AuthController.REFRESH_TOKEN_COOKIE_NAME))).thenReturn(refreshTokenCookie.getValue());
            when(authUtil.getUserIdFromRefreshToken(any())).thenReturn("testId");
            when(userService.getUserFromId("testId")).thenReturn(testUser);
            when(authUtil.isRefreshTokenValid(any())).thenReturn(true);
            when(authUtil.isRefreshTokenAuthVersionValid(any(), any())).thenReturn(true);

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new jakarta.servlet.http.Cookie("JWT_Refresh_Token", "valid_token")))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value("User refresh successful"))
                    .andExpect(jsonPath("$.id").value("testId"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.provider").value("LOCAL"))
                    .andExpect(cookie().exists("JWT_Access_Token"))
                    .andExpect(cookie().exists("JWT_Refresh_Token"));
        }

        @Test
        public void testRefreshFailureNoToken() throws Exception {
            when(authUtil.getTokenFromCookie(any(), eq(AuthController.REFRESH_TOKEN_COOKIE_NAME))).thenReturn(null);

            mockMvc.perform(post("/auth/refresh"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Refresh token not found"));
        }

        @Test
        public void testRefreshFailureInvalidToken() throws Exception {
            when(authUtil.getTokenFromCookie(any(), eq(AuthController.REFRESH_TOKEN_COOKIE_NAME))).thenReturn("invalid_token");
            when(authUtil.getUserIdFromRefreshToken(any())).thenReturn("testId");
            when(userService.getUserFromId("testId")).thenReturn(testUser);
            when(authUtil.isRefreshTokenValid(any())).thenReturn(false);
            when(authUtil.isRefreshTokenAuthVersionValid(any(), any())).thenReturn(false);

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new jakarta.servlet.http.Cookie("JWT_Refresh_Token", "invalid_token")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Refresh token expired or invalid"));
        }
    }

    private AccessToken createAccessToken() {
        return new AccessToken("testId", "divyematsBHqHUxi6QD5D811iWH7qNxUW9U/QboseFw=", 3600);
    }

    private RefreshToken createRefreshToken() {
        return new RefreshToken("testId", 1, "divyematsBHqHUxi6QD5D811iWH7qNxUW9U/QboseFw=", 86400);
    }

    public void setupTokenMocks() {
        AccessToken accessToken = createAccessToken();
        RefreshToken refreshToken = createRefreshToken();

        when(authUtil.createAccessToken("testId")).thenReturn(accessToken);
        when(authUtil.createRefreshToken("testId", 1)).thenReturn(refreshToken);

        Cookie accessTokenCookie = new Cookie("JWT_Access_Token", accessToken.token);
        refreshTokenCookie = new Cookie("JWT_Refresh_Token", refreshToken.token);

        when(authUtil.createTokenCookie("JWT_Access_Token", accessToken.token)).thenReturn(accessTokenCookie);
        when(authUtil.createTokenCookie("JWT_Refresh_Token", refreshToken.token)).thenReturn(refreshTokenCookie);
    }
}