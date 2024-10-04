package com.org.project.controller;

import com.org.project.model.auth.AccessToken;
import com.org.project.model.auth.RegisterRequest;
import com.org.project.model.User;
import com.org.project.model.auth.RegisterRequest;
import com.org.project.service.UserService;
import com.org.project.component.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        User savedUser = userService.registerUser(registerRequest);

        Integer userId = savedUser.getId();

        AccessToken accessToken = authUtil.createAccessToken(userId);

        return new ResponseEntity<>(accessToken.token, HttpStatus.CREATED);
    }

}
