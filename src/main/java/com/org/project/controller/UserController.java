package com.org.project.controller;

import com.org.project.model.User;
import com.org.project.security.Secured;
import com.org.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Secured
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> userInfo(HttpServletRequest request) {
        String user_id = (String) request.getAttribute("user_id");

        User user = userService.getUserFromId(user_id);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user_id", user.getId());
        responseBody.put("name", user.getName());
        responseBody.put("email", user.getEmail());
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
