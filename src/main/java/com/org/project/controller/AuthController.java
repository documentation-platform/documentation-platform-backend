package com.org.project.controller;

import com.org.project.dto.LoginRequestDTO;
import com.org.project.dto.RegisterRequestDTO;
import com.org.project.exception.AccountExistsException;
import com.org.project.exception.UnauthorizedException;
import com.org.project.model.auth.*;
import com.org.project.model.User;
import com.org.project.security.Secured;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "JWT_Access_Token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "JWT_Refresh_Token";

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
            return createErrorResponse("Login failed", HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(HttpServletResponse response, @Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        try {
            User savedUser = userService.registerUser(registerRequestDTO);
            setAuthCookies(response, savedUser);
            return createResponse("User successfully registered", HttpStatus.CREATED, savedUser);
        } catch (AccountExistsException e) {
            return createErrorResponse("User already exists", HttpStatus.CONFLICT);
        } catch (UnauthorizedException e) {
            return createErrorResponse("Registration failed", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = authUtil.getTokenFromCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        String refreshToken = authUtil.getTokenFromCookie(request, REFRESH_TOKEN_COOKIE_NAME);

        if (accessToken != null && authUtil.isAccessTokenValid(accessToken)) {
            return createErrorResponse("Access token found, no need to refresh", HttpStatus.TOO_EARLY);
        }

        if (refreshToken == null) {
            return createErrorResponse("Refresh token not found", HttpStatus.UNAUTHORIZED);
        }

        if (!authUtil.isRefreshTokenValid(refreshToken)) {
            return createErrorResponse("Refresh token expired or invalid", HttpStatus.UNAUTHORIZED);
        }

        String userId = authUtil.getUserIdFromRefreshToken(refreshToken);
        User user = userService.getUserFromId(userId);

        if (!authUtil.isRefreshTokenAuthVersionValid(refreshToken, user.getAuthVersion())) {
            return createErrorResponse("Refresh token version invalid", HttpStatus.UNAUTHORIZED);
        }

        setAuthCookies(response, user);
        return createResponse("User refresh successful", HttpStatus.ACCEPTED, user);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpServletRequest request) {
        List<Boolean> requestAuthorizedList = authUtil.isRequestAuthorized(request, ACCESS_TOKEN_COOKIE_NAME);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("isAuthorized", requestAuthorizedList.get(0));
        responseBody.put("previouslyAuthorized", requestAuthorizedList.get(1));

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie accessExpireCookie = createCookie(ACCESS_TOKEN_COOKIE_NAME, "");
        Cookie refreshExpireCookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, "");

        accessExpireCookie.setMaxAge(0);
        refreshExpireCookie.setMaxAge(0);

        response.addCookie(accessExpireCookie);
        response.addCookie(refreshExpireCookie);
        return new ResponseEntity<>("Logged out", HttpStatus.ACCEPTED);
    }

    @Secured
    @PostMapping("/logout_all")
    public ResponseEntity<String> logoutAll(HttpServletRequest request, HttpServletResponse response) {
        String userId = (String) request.getAttribute("user_id");

        try {
            userService.updateAuthVersion(userId);
            Cookie accessExpireCookie = createCookie(ACCESS_TOKEN_COOKIE_NAME, "");
            Cookie refreshExpireCookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, "");

            accessExpireCookie.setMaxAge(0);
            refreshExpireCookie.setMaxAge(0);

            response.addCookie(accessExpireCookie);
            response.addCookie(refreshExpireCookie);
            return new ResponseEntity<>("Logged out from all devices", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>("Logout failed", HttpStatus.UNAUTHORIZED);
        }
    }

    private void setAuthCookies(HttpServletResponse response, User user) {
        AccessToken accessToken = authUtil.createAccessToken(user.getId());
        RefreshToken refreshToken = authUtil.createRefreshToken(user.getId(), user.getAuthVersion());

        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken.token));
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken.token));
    }

    private Cookie createCookie(String name, String value) {
        return authUtil.createTokenCookie(name, value);
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