package com.org.project.controller;

import com.org.project.dto.structure.FolderStructureDTO;
import com.org.project.model.Folder;
import com.org.project.security.organization.OrganizationEditor;
import com.org.project.security.organization.OrganizationViewer;
import com.org.project.service.FolderService;
import jakarta.persistence.EntityManager;
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

    @Autowired
    private EntityManager entityManager;

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
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while moving the folder"));
        }
    }

    @OrganizationEditor
    @PatchMapping("/{folder_id}/organization/{organization_id}/move")
    public ResponseEntity<Map<String, Object>> moveOrganizationFolder(
            @PathVariable("folder_id") String folderId,
            @PathVariable("organization_id") String organizationId,
            @RequestParam("parent_folder_id") String parentFolderId,
            HttpServletRequest request
    ) {
        try {
            Folder updatedFolder = folderService.moveOrganizationFolder(organizationId, folderId, parentFolderId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Folder moved successfully");
            response.put("folder_id", updatedFolder.getId());
            response.put("parent_folder_id", updatedFolder.getParentFolder().getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while moving the folder"));
        }
    }

    @OrganizationViewer
    @GetMapping("/root/organization/{organization_id}/structure")
    public ResponseEntity<Map<String, Object>> getOrganizationRootFolderStructure(
            @PathVariable("organization_id") String organizationId,
            HttpServletRequest request
    ) {
        try {
            Folder rootFolder = folderService.getRootOrganizationFolder(organizationId);
            FolderStructureDTO folderStructure = folderService.getFolderStructure(rootFolder);
            Map<String, Object> response = new HashMap<>();
            response.put("structure_folder_id", folderStructure.getStructureFolderId());
            response.put("folders", folderStructure.getFolders());
            response.put("files", folderStructure.getFiles());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while fetching the folder structure"));
        }
    }

    @OrganizationViewer
    @GetMapping("/{folder_id}/organization/{organization_id}/structure")
    public ResponseEntity<Map<String, Object>> getOrganizationFolderStructure(
            @PathVariable("folder_id") String folderId,
            @PathVariable("organization_id") String organizationId,
            HttpServletRequest request
    ) {
        try {
            boolean canAccess = folderService.canOrganizationAccessFolder(organizationId, folderId);

            if (!canAccess) {
                return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to access this folder"));
            }

            FolderStructureDTO folderStructure = folderService.getFolderStructure(entityManager.getReference(Folder.class, folderId));
            Map<String, Object> response = new HashMap<>();
            response.put("structure_folder_id", folderStructure.getStructureFolderId());
            response.put("folders", folderStructure.getFolders());
            response.put("files", folderStructure.getFiles());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while fetching the folder structure"));
        }
    }

    @GetMapping("/root/user/structure")
    public ResponseEntity<Map<String, Object>> getUserRootFolderStructure(
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("user_id");

        try {
            Folder rootFolder = folderService.getRootUserFolder(userId);
            FolderStructureDTO folderStructure = folderService.getFolderStructure(rootFolder);
            Map<String, Object> response = new HashMap<>();
            response.put("structure_folder_id", folderStructure.getStructureFolderId());
            response.put("folders", folderStructure.getFolders());
            response.put("files", folderStructure.getFiles());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while fetching the folder structure"));
        }
    }

    @GetMapping("/{folder_id}/user/structure")
    public ResponseEntity<Map<String, Object>> getUserFolderStructure(
            @PathVariable("folder_id") String folderId,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("user_id");

        try {
            boolean canAccess = folderService.canUserAccessFolder(userId, folderId);

            if (!canAccess) {
                return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to access this folder"));
            }

            FolderStructureDTO folderStructure = folderService.getFolderStructure(entityManager.getReference(Folder.class, folderId));
            Map<String, Object> response = new HashMap<>();
            response.put("structure_folder_id", folderStructure.getStructureFolderId());
            response.put("folders", folderStructure.getFolders());
            response.put("files", folderStructure.getFiles());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while fetching the folder structure"));
        }
    }
}
