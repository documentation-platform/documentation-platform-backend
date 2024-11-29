package com.org.project.controller;

import com.org.project.model.*;
import com.org.project.repository.*;
import com.org.project.security.organization.OrganizationAdmin;
import com.org.project.security.organization.OrganizationEditor;
import com.org.project.security.organization.OrganizationViewer;
import com.org.project.service.DocumentService;
import com.org.project.service.OrganizationService;
import com.org.project.util.OrganizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationUserRelationRepository OrganizationUserRelationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AccessRepository AccessRepository;

    @Autowired
    private InviteRepository InviteRepository;

    @Autowired
    private UserRepository UserRepository;

    @Value("${WEB_APPLICATION_URL}")
    private String baseUrl;

    //get a simple list of all organizations
    @GetMapping("/get-all")
    public ResponseEntity<Map<String, Object>> getOrganizations(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("organizations", organizationRepository.findAll());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getOrganizationsByUserId(HttpServletRequest request) {
        String userId = (String) request.getAttribute("user_id");

        // Fetch all OrganizationUserRelation records for the given userId
        List<OrganizationUserRelation> userRelations = OrganizationUserRelationRepository.findByUserId(userId);

        if (userRelations.isEmpty()) {
            return new ResponseEntity<>(Map.of("organizations", Collections.emptyList()), HttpStatus.OK);
        }

        // Extract the organizationIds from the relations
        List<String> organizationIds = userRelations.stream()
                .map(OrganizationUserRelation::getOrganizationId)
                .collect(Collectors.toList());

        // Fetch the organizations based on the organizationIds
        List<Organization> organizations = organizationRepository.findAllById(organizationIds);

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> organizationDetails = new ArrayList<>();

        for (Organization organization : organizations) {
            // Fetch the access level for the user in this organization
            OrganizationUserRelation userRelation = userRelations.stream()
                    .filter(relation -> relation.getOrganizationId().equals(organization.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("User not found in organization"));

            // Create a map for this organization's details
            Map<String, Object> orgDetails = new HashMap<>();
            orgDetails.put("id", organization.getId());
            orgDetails.put("name", organization.getName());
            orgDetails.put("access", OrganizationUtil.getAccessLevel(userRelation.getAccessId()));
            orgDetails.put("accessId", userRelation.getAccessId());
            orgDetails.put("createdAt", organization.getCreatedAt());
            orgDetails.put("updatedAt", organization.getUpdatedAt());

            organizationDetails.add(orgDetails);
        }

        response.put("organizations", organizationDetails);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @OrganizationViewer
    @GetMapping("/{org_id}/info")
    public ResponseEntity<Map<String, Object>> getOrganizationInfo(
            @PathVariable("org_id") String organizationId,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("user_id");

        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        OrganizationUserRelation relation = OrganizationUserRelationRepository.findByUserIdAndOrganizationId(userId, organizationId);

        if (organization == null || relation == null) {
            return new ResponseEntity<>(Map.of("error", "Organization not found"), HttpStatus.NOT_FOUND);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("name", organization.getName());
        response.put("access_name", OrganizationUtil.getAccessLevel(relation.getAccessId()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Create a new organization
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrganization(HttpServletRequest securedRequest, @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String userId = (String) securedRequest.getAttribute("user_id");

        if (name == null || name.isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "Name is required"), HttpStatus.BAD_REQUEST);
        }

        //create the org and grab the id of new org
        Organization newOrganization = new Organization(name);
        organizationRepository.save(newOrganization);
        String organizationId = newOrganization.getId();

        organizationService.createOrganizationRootFolder(organizationId);

        Map<String, Object> response = new HashMap<>();
        response.put("organization", newOrganization);

        //Add the user to the org they just made as an admin
        OrganizationUserRelation relation = new OrganizationUserRelation();
        relation.setUserId(userId);
        relation.setOrganizationId(organizationId);
        relation.setAccessId(1);
        OrganizationUserRelationRepository.save(relation);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @OrganizationAdmin
    @PostMapping("/{org_id}/create-invite-link")
    public ResponseEntity<Map<String, Object>> createInviteLink(
            @PathVariable("org_id") String organizationId,
            @RequestParam("accessId") Integer accessId) {

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

    // API to accept the invite
    // Accept invite endpoint
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

    @OrganizationAdmin
    @GetMapping("/{org_id}/get-invite-links")
    public ResponseEntity<Map<String, Object>> getInviteLinks(@PathVariable("org_id") String organizationId) {
        // Validate if the organization exists
        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        if (organization == null) {
            return new ResponseEntity<>(Map.of("error", "Organization not found"), HttpStatus.NOT_FOUND);
        }

        // Retrieve all invites for the organization
        List<Invite> invites = InviteRepository.findByOrganization(organization);

        // Check if there are any invites
        if (invites.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "No invites found for this organization"), HttpStatus.OK);
        }

        // Prepare the response
        List<Map<String, Object>> inviteLinks = invites.stream()
                .map(invite -> {
                    Map<String, Object> inviteInfo = new HashMap<>();
                    inviteInfo.put("inviteToken", invite.getId());  // Invite Token
                    inviteInfo.put("accessId", invite.getAccess().getId());  // Access ID
                    inviteInfo.put("inviteLink", baseUrl + "invite?token=" + invite.getId());  // Generated invite link
                    inviteInfo.put("currentCount", invite.getCurrentCount());  // Current invite count
                    inviteInfo.put("maxCount", invite.getMaxCount());  // Max count
                    inviteInfo.put("expiresAt", invite.getExpiresAt());  // Expiration (if set)
                    return inviteInfo;
                })
                .collect(Collectors.toList());

        // Prepare the response body
        Map<String, Object> response = new HashMap<>();
        response.put("organizationId", organizationId);
        response.put("inviteLinks", inviteLinks);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @OrganizationAdmin
    @GetMapping("/{org_id}/get-settings")
    public ResponseEntity<Map<String, Object>> getOrganizationSettings(@PathVariable("org_id") String organizationId,HttpServletRequest securedRequest) {

        // Get the user ID from the secured request
        String userId = (String) securedRequest.getAttribute("user_id");


            // Validate if the organization exists
            Organization organization = organizationRepository.findById(organizationId).orElse(null);
            if (organization == null) {
                return new ResponseEntity<>(Map.of("error", "Organization not found"), HttpStatus.NOT_FOUND);
            }



            // Fetch invite links for the organization
            List<Invite> invites = InviteRepository.findByOrganization(organization);
            List<Map<String, Object>> inviteLinks = invites.stream()
                    .map(invite -> {
                        Map<String, Object> inviteInfo = new HashMap<>();
                        inviteInfo.put("inviteToken", invite.getId());

                        inviteInfo.put("inviteLink", baseUrl + "invite?token=" + invite.getId());
                        inviteInfo.put("currentCount", invite.getCurrentCount());
                        inviteInfo.put("maxCount", invite.getMaxCount());
                        inviteInfo.put("expiresAt", invite.getExpiresAt());
                        return inviteInfo;
                    })
                    .collect(Collectors.toList());

            // Fetch members with their permission levels
            List<OrganizationUserRelation> userRelations = OrganizationUserRelationRepository.findByOrganizationId(organizationId);
            List<Map<String, Object>> members = userRelations.stream()
                    .map(relation -> {
                        User user = UserRepository.findById(relation.getUserId());
                        Map<String, Object> memberInfo = new HashMap<>();
                        memberInfo.put("userId", relation.getUserId());
                        memberInfo.put("name", user != null ? user.getName() : "Unknown");
                        memberInfo.put("email", user != null ? user.getEmail() : "Unknown");
                        memberInfo.put("permissionLevel", OrganizationUtil.getAccessLevel(relation.getAccessId()));
                        memberInfo.put("joinedAt", relation.getCreatedAt());
                        return memberInfo;
                    })
                    .collect(Collectors.toList());

            // Fetch the relation of the current user with the organization
            OrganizationUserRelation currentUserRelation = OrganizationUserRelationRepository
                    .findByUserIdAndOrganizationId(userId, organizationId);


            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("organizationName", organization.getName());
            response.put("currentUserJoinedAt", currentUserRelation.getCreatedAt());
            response.put("members", members);
            response.put("inviteLinks", inviteLinks);

            return ResponseEntity.ok(response);
        }


    }






