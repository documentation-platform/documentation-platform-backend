package com.org.project.controller;

import com.org.project.dto.LoginRequestDTO;
import com.org.project.dto.RegisterRequestDTO;
import com.org.project.exception.UnauthorizedException;
import com.org.project.model.auth.*;
import com.org.project.model.User;
import com.org.project.service.UserService;
import com.org.project.service.AuthService;
import com.org.project.util.AuthUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "JWT_Access_Token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "JWT_Refresh_Token";

    private final UserService userService;
    private final AuthService authService;
    private final AuthUtil authUtil;

    @Autowired
    public AuthController(UserService userService, AuthService authService, AuthUtil authUtil) {
        this.userService = userService;
        this.authService = authService;
        this.authUtil = authUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(HttpServletResponse response, @Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            User user = authService.login(loginRequestDTO);
            setAuthCookies(response, user);
            return createResponse("Login successful", HttpStatus.ACCEPTED, user);
        } catch (UnauthorizedException e) {
            return createErrorResponse("Login failed", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(HttpServletResponse response, @Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        try {
            User savedUser = userService.registerUser(registerRequestDTO);
            setAuthCookies(response, savedUser);
            return createResponse("User successfully registered", HttpStatus.CREATED, savedUser);
        } catch (UnauthorizedException e) {
            return createErrorResponse("Registration failed", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> refreshCookie = Optional.ofNullable(authUtil.getRefreshCookie(request));

        if (refreshCookie.isEmpty()) {
            return createErrorResponse("Refresh token not found", HttpStatus.UNAUTHORIZED);
        }

        String token = refreshCookie.get().getValue();
        String userId = authUtil.getUserIdFromToken(token);
        User user = userService.getUserFromId(userId);

        if (!authUtil.isTokenValid(token, user.getAuthVersion())) {
            return createErrorResponse("Refresh token expired or invalid", HttpStatus.UNAUTHORIZED);
        }

        setAuthCookies(response, user);
        return createResponse("User refresh successful", HttpStatus.ACCEPTED, user);
    }

    private void setAuthCookies(HttpServletResponse response, User user) {
        AccessToken accessToken = authUtil.createAccessToken(user.getId());
        RefreshToken refreshToken = authUtil.createRefreshToken(user.getId(), user.getAuthVersion());

        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken.token, accessToken.expiration));
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken.token, refreshToken.expiration));
    }

    private Cookie createCookie(String name, String value, int expiration) {
        return authUtil.createTokenCookie(name, value, expiration);
    }

    private ResponseEntity<Map<String, Object>> createResponse(String message, HttpStatus status, User user) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", message);
        responseBody.put("id", user.getId());
        responseBody.put("email", user.getEmail());
        responseBody.put("provider", user.getProvider());
        if (status == HttpStatus.CREATED) {
            responseBody.put("createdAt", user.getCreatedAt());
        }
        return new ResponseEntity<>(responseBody, status);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", message);
        return new ResponseEntity<>(responseBody, status);
    }
}