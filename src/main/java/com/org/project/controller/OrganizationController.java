package com.org.project.controller;

import com.org.project.model.Access;
import com.org.project.model.Invite;
import com.org.project.model.User;
import com.org.project.model.Organization;
import com.org.project.model.OrganizationUserRelation;
import com.org.project.repository.*;
import com.org.project.security.Secured;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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


    // Create an invite link

    @Autowired
    private AccessRepository AccessRepository;

    @Autowired
    private InviteRepository InviteRepository;

    @Value("${WEB_APPLICATION_URL}")
    private String baseUrl;

    // Create an invite link
    @PostMapping("/create-invite-link")
    public ResponseEntity<Map<String, Object>> createInviteLink(@RequestBody Map<String, Integer> request) {
        Integer organizationId = request.get("organizationId");
        Integer accessId = request.get("accessId");

        // Validate if the organization exists
        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        if (organization == null) {
            return new ResponseEntity<>(Map.of("error", "Organization not found"), HttpStatus.NOT_FOUND);
        }

        // Validate if the access exists
        Access access = AccessRepository.findById(accessId).orElse(null);
        if (access == null) {
            return new ResponseEntity<>(Map.of("error", "Access not found"), HttpStatus.NOT_FOUND);
        }

        // Generate a unique invite token (using UUID for simplicity)
        String inviteToken = UUID.randomUUID().toString();

        // Create the invite object
        Invite invite = new Invite();
        invite.setId(inviteToken);  // Set the UUID string as the invite ID
        invite.setAccess(access);  // Link the invite to the access
        invite.setOrganization(organization);  // Link the invite to the organization
        invite.setCurrentCount(0);  // Default current_count
        invite.setMaxCount(5);  // Example: set max_count
        invite.setExpiresAt(null);  // Optional: expiration can be set

        // Save the invite
        InviteRepository.save(invite);

        // Construct the invite link using the baseApiUrl loaded from .env
        String inviteLink = baseUrl + "invite?token=" + inviteToken;

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        response.put("organizationId", organizationId);
        response.put("accessId", accessId);
        response.put("inviteToken", inviteToken);
        response.put("inviteLink", inviteLink);  // Include the generated invite link

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Autowired
    private OrganizationUserRelationRepository OrganizationUserRelationRepository;

    // API to accept the invite
    // Accept invite endpoint
    @Secured
    @PostMapping("/accept-invite")
    public ResponseEntity<Map<String, Object>> acceptInvite(HttpServletRequest securedRequest, @RequestBody Map<String, String> request) {
        String userId = (String) securedRequest.getAttribute("user_id");
        String inviteToken = request.get("token");


        Invite invite = InviteRepository.findById(inviteToken).orElse(null);
        if (invite == null) {
            return new ResponseEntity<>(Map.of(
                    "error", "Invalid invite token",
                    "providedToken", inviteToken
            ), HttpStatus.NOT_FOUND);
        }

        // Check if the invite's usage limit is reached
        if (invite.getCurrentCount() >= invite.getMaxCount()) {
            return new ResponseEntity<>(Map.of("error", "Invite usage limit reached"), HttpStatus.FORBIDDEN);
        }

        // Check if user already exists in organization_user_relation
        OrganizationUserRelation existingRelation = OrganizationUserRelationRepository.findByUserIdAndOrganizationId(userId, invite.getOrganization().getId());
        if (existingRelation != null) {
            return new ResponseEntity<>(Map.of("error", "User is already part of the organization"), HttpStatus.CONFLICT);
        }

        // Create the relation between the user and organization with the access level
        OrganizationUserRelation relation = new OrganizationUserRelation();
        relation.setUserId(userId);
        relation.setOrganizationId(invite.getOrganization().getId());
        relation.setAccessId(invite.getAccess().getId());
        OrganizationUserRelationRepository.save(relation);

        // Update the current count of invite usage
        invite.setCurrentCount(invite.getCurrentCount() + 1);
        InviteRepository.save(invite);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Invite accepted");
        response.put("organizationId", invite.getOrganization().getId());
        response.put("userId", userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}





