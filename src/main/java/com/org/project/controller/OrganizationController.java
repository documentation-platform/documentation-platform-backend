package com.org.project.controller;

import com.org.project.repository.OrganizationRepository;
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
@RequestMapping("/organization")
public class OrganizationController {

    @Autowired
    private OrganizationRepository organizationRepository;

    //get a simple list of all organizations
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getOrganizations(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("organizations", organizationRepository.findAll());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
}