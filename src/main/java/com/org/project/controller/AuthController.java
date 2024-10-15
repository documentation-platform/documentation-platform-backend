package com.org.project.controller;

import com.org.project.exception.UnauthorizedException;
import com.org.project.model.auth.AccessToken;
import com.org.project.model.auth.LoginRequestDTO;
import com.org.project.model.auth.RefreshToken;
import com.org.project.model.auth.RegisterRequestDTO;
import com.org.project.model.User;
import com.org.project.service.UserService;
import com.org.project.service.AuthService;
import com.org.project.component.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
    public ResponseEntity<String> login(HttpServletResponse response, @Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            User user = authService.login(loginRequestDTO);

            String userId = user.getId();
            Integer authVersion = user.getAuthVersion();

            AccessToken accessToken = authUtil.createAccessToken(userId);
            RefreshToken refreshToken = authUtil.createRefreshToken(userId, authVersion);

            Cookie access_cookie = authUtil.createTokenCookie("JWT_Access_Token", accessToken.token, accessToken.expiration);
            Cookie refresh_cookie = authUtil.createTokenCookie("JWT_Refresh_Token", refreshToken.token, refreshToken.expiration);

            response.addCookie(access_cookie);
            response.addCookie(refresh_cookie);

            return ResponseEntity.ok("Login successful");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(HttpServletResponse response, @Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        User savedUser = userService.registerUser(registerRequestDTO);
        String userId = savedUser.getId();
        Integer authVersion = savedUser.getAuthVersion();

        AccessToken accessToken = authUtil.createAccessToken(userId);
        RefreshToken refreshToken = authUtil.createRefreshToken(userId, authVersion);

        Cookie access_cookie = authUtil.createTokenCookie("JWT_Access_Token", accessToken.token, accessToken.expiration);
        Cookie refresh_cookie = authUtil.createTokenCookie("JWT_Refresh_Token", refreshToken.token, refreshToken.expiration);

        response.addCookie(access_cookie);
        response.addCookie(refresh_cookie);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "User successfully registered");
        responseBody.put("id", savedUser.getId());
        responseBody.put("email", savedUser.getEmail());
        responseBody.put("provider", savedUser.getProvider());
        responseBody.put("createdAt", savedUser.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }
}
