package com.org.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/private/user")
public class UserController {
    @GetMapping("/info")
    public ResponseEntity<String> userInfo(HttpServletRequest request) {
        String user_id = (String) request.getAttribute("user_id");
        return new ResponseEntity<String>("User ID: " + user_id, HttpStatus.OK);
    }
}
