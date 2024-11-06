package com.org.project.controller;

import com.org.project.model.Organization;
import com.org.project.repository.OrganizationRepository;
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

    // Create a new organization
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrganization(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "Name is required"), HttpStatus.BAD_REQUEST);
        }

        Organization newOrganization = new Organization(name);
        organizationRepository.save(newOrganization);

        Map<String, Object> response = new HashMap<>();
        response.put("organization", newOrganization);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


}