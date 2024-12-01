package com.org.project.controller;
import org.springframework.web.bind.annotation.*;
import com.org.project.model.*;
import com.org.project.repository.*;
import com.org.project.security.organization.OrganizationAdmin;
import com.org.project.security.organization.OrganizationEditor;
import com.org.project.security.organization.OrganizationViewer;
import com.org.project.util.OrganizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.*;
import com.org.project.model.Invite;
import com.org.project.repository.InviteRepository;
import java.util.Optional;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private OrganizationUserRelationRepository organizationUserRelationRepository;

    @OrganizationAdmin
    @DeleteMapping("organization/{org_id}/delete-invite-link")
    public ResponseEntity<Map<String, Object>> deleteInviteLink(
            @PathVariable("org_id") String organizationId,
            @RequestParam("token") String inviteToken) {

        // Find and delete the invite
        inviteRepository.deleteById(inviteToken);

        // Respond with success
        return new ResponseEntity<>(
                Map.of(
                        "message", "Invite link deleted successfully"
                ),
                HttpStatus.OK
        );
    }

    @OrganizationAdmin
    @DeleteMapping("organization/{org_id}/kick-org-member")
    public ResponseEntity<Map<String, Object>> kickOrgMember(
            @PathVariable("org_id") String organizationId,
            @RequestParam("user_id") String userId) {

        // Find the organization-user relation
        OrganizationUserRelation relation = organizationUserRelationRepository.findByUserIdAndOrganizationId(userId, organizationId);

        if (relation == null) {
            return new ResponseEntity<>(
                    Map.of(
                            "error", "User not found in the organization",
                            "organizationId", organizationId,
                            "userId", userId
                    ),
                    HttpStatus.NOT_FOUND
            );
        }

        // Delete the organization-user relation
        organizationUserRelationRepository.delete(relation);

        // Respond with success
        return new ResponseEntity<>(
                Map.of(
                        "message", "User removed from organization successfully",
                        "organizationId", organizationId,
                        "userId", userId
                ),
                HttpStatus.OK
        );
    }


}

