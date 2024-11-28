package com.org.project.controller;

import com.org.project.model.Folder;
import com.org.project.security.organization.OrganizationEditor;
import com.org.project.service.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/organization/{org_id}/create")
    public ResponseEntity<Map<String, Object>> createFolder(
            @PathVariable("org_id") String organizationId,
            HttpServletRequest securedRequest
    ) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/{folder_id}/name")
    public ResponseEntity<Map<String, Object>> updateFolderName(
            @PathVariable("folder_id") String folderId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request
    ){
        String userId = (String) request.getAttribute("user_id");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{folder_id}/delete")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable("document_id") String documentId,
            HttpServletRequest request
    ){
        String userId = (String) request.getAttribute("user_id");

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
