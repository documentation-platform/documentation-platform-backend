package com.org.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {
    @GetMapping
    public ResponseEntity<String> healthResponse() {
        return new ResponseEntity<String>("API Healthy", HttpStatus.OK);
    }
}
