package com.org.project.controller;

import com.org.project.model.*;
import com.org.project.repository.*;
import com.org.project.security.organization.OrganizationAdmin;
import com.org.project.security.organization.OrganizationEditor;
import com.org.project.service.DocumentService;
import com.org.project.service.OrganizationService;
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

        // If no relations are found, return a message indicating no organizations
        if (userRelations.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "No organizations found for this user"), HttpStatus.NOT_FOUND);
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

            // Fetch the access name for this relation
            Access access = AccessRepository.findById(userRelation.getAccessId())
                    .orElseThrow(() -> new RuntimeException("Access level not found"));

            // Create a map for this organization's details
            Map<String, Object> orgDetails = new HashMap<>();
            orgDetails.put("id", organization.getId());
            orgDetails.put("name", organization.getName());
            orgDetails.put("access", access.getName());
            orgDetails.put("accessId", access.getId());
            orgDetails.put("createdAt", organization.getCreatedAt());
            orgDetails.put("updatedAt", organization.getUpdatedAt());

            // If the user is an admin (accessId == 1), add the members list
            if (access.getId() == 1) {
                // Fetch all members of the organization
                List<OrganizationUserRelation> orgRelations = OrganizationUserRelationRepository.findByOrganizationId(organization.getId());

                // Build the members list
                List<Map<String, Object>> members = new ArrayList<>();
                for (OrganizationUserRelation orgRelation : orgRelations) {
                    String memberId = orgRelation.getUserId();
                    Access memberAccess = AccessRepository.findById(orgRelation.getAccessId())
                            .orElseThrow(() -> new RuntimeException("Access level not found for member"));

                    // Fetch the username based on the userId
                    User member = UserRepository.findById(memberId);

                    Map<String, Object> memberDetails = new HashMap<>();
                    memberDetails.put("userId", memberId);
                    memberDetails.put("userName", member.getName());
                    memberDetails.put("access", memberAccess.getName());

                    members.add(memberDetails);
                }

                orgDetails.put("members", members);  // Add the members list
            }

            // Add the organization details to the response
            organizationDetails.add(orgDetails);
        }

        response.put("organizations", organizationDetails);
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

    @OrganizationEditor
    @PostMapping("/{org_id}/create-document")
    public ResponseEntity<Map<String, Object>> createDocument(
            @PathVariable("org_id") String organizationId,
            HttpServletRequest securedRequest
    ) {
        String userId = (String) securedRequest.getAttribute("user_id");
        File newOrganizationDocument = documentService.createOrganizationDocument(userId, organizationId);

        if (newOrganizationDocument == null) {
            return new ResponseEntity<>(Map.of("error", "Failed to create document"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("document_id", newOrganizationDocument.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{org_id}/recent-documents")
    public ResponseEntity<Map<String, Object>> getRecentDocuments(
            @PathVariable("org_id") String organizationId,
            @RequestParam("page") Integer page_number
    ) {
//        List<File> recentDocuments = documentService.getRecentDocuments(organizationId, page_number);
//
//        if (recentDocuments.isEmpty()) {
//            return new ResponseEntity<>(Map.of("message", "No documents found"), HttpStatus.NOT_FOUND);
//        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}





