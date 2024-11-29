package com.org.project.controller;

import com.org.project.model.Folder;
import com.org.project.security.organization.OrganizationEditor;
import com.org.project.service.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @PostMapping("/user/create")
    public ResponseEntity<Map<String, Object>> createUserFolder(
            @RequestParam("name") String folderName,
            @RequestParam(value = "parent_folder_id", required = false) String parentFolderId,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("user_id");

        try {
            Folder newFolder = folderService.createUserFolder(userId, folderName, parentFolderId);
            return ResponseEntity.status(201).body(Map.of("folder_id", newFolder.getId()));
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while creating the folder"));
        }
    }

    @OrganizationEditor
    @PostMapping("/organization/{organization_id}/create")
    public ResponseEntity<Map<String, Object>> createOrganizationFolder(
            @PathVariable("organization_id") String organizationId,
            @RequestParam("name") String folderName,
            @RequestParam(value = "parent_folder_id", required = false) String parentFolderId,
            HttpServletRequest request
    ) {
        try {
            Folder newFolder = folderService.createOrganizationFolder(organizationId, folderName, parentFolderId);
            return ResponseEntity.status(201).body(Map.of("folder_id", newFolder.getId()));
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while creating the folder"));
        }
    }

    @PatchMapping("/{folder_id}/user/move")
    public ResponseEntity<Map<String, Object>> moveUserFolder(
            @PathVariable("folder_id") String folderId,
            @RequestParam("parent_folder_id") String parentFolderId,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("user_id");

        try {
            Folder updatedFolder = folderService.moveUserFolder(userId, folderId, parentFolderId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Folder moved successfully");
            response.put("folder_id", updatedFolder.getId());
            response.put("parent_folder_id", updatedFolder.getParentFolder().getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while moving the folder"));
        }
    }
}
