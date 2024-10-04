package com.org.project.controller;

import com.org.project.model.auth.AccessToken;
import com.org.project.model.auth.RefreshToken;
import com.org.project.model.auth.RegisterRequest;
import com.org.project.model.User;
import com.org.project.service.UserService;
import com.org.project.component.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthUtil authUtil;

    @Autowired
    public AuthController(UserService userService, AuthUtil authUtil) {
        this.userService = userService;
        this.authUtil = authUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(HttpServletResponse response, @RequestBody RegisterRequest registerRequest) {
        User savedUser = userService.registerUser(registerRequest);
        Integer userId = savedUser.getId();
        Integer authVersion = savedUser.getAuthVersion();
        AccessToken accessToken = authUtil.createAccessToken(userId);
        RefreshToken refreshToken = authUtil.createRefreshToken(userId, authVersion);

        Cookie access_cookie = new Cookie("JWT_Access_Token", accessToken.token);
        access_cookie.setHttpOnly(true);
        access_cookie.setSecure(true);
        access_cookie.setPath("/");
        access_cookie.setMaxAge(accessToken.expiration);
        response.addCookie(access_cookie);

        Cookie refresh_cookie = new Cookie("JWT_Refresh_Token", refreshToken.token);
        refresh_cookie.setHttpOnly(true);
        refresh_cookie.setSecure(true);
        refresh_cookie.setPath("/");
        refresh_cookie.setMaxAge(refreshToken.expiration);
        response.addCookie(refresh_cookie);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}
