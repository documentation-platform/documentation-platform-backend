package com.org.project.controller;

import com.org.project.model.File;
import com.org.project.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/{document_id}/view")
    public ResponseEntity<Map<String, Object>> viewDocument(
            @PathVariable("document_id") String documentId,
            HttpServletRequest request
    ){
        String userId = (String) request.getAttribute("user_id");

        if (!documentService.canUserViewDocument(userId, documentId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to view this document"));
        }

        String documentContent = documentService.getDocumentContent(documentId);

        return ResponseEntity.ok(Map.of("content", documentContent));
    }

    @PatchMapping("/{document_id}/content")
    public ResponseEntity<Map<String, Object>> updateDocumentContent(
            @PathVariable("document_id") String documentId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request
    ){
        String userId = (String) request.getAttribute("user_id");

        if (!documentService.canUserEditDocument(userId, documentId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to edit this document"));
        }

        String newContent = (String) requestBody.get("content");

        boolean fileWasUpdated = documentService.updateDocumentContent(userId, documentId, newContent);

        if (!fileWasUpdated) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while updating the document"));
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("message", "Document updated successfully");
        response.put("file_id", documentId);
        response.put("updated_timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{document_id}/name")
    public ResponseEntity<Map<String, Object>> updateDocumentName(
            @PathVariable("document_id") String documentId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request
    ){
        String userId = (String) request.getAttribute("user_id");

        if (!documentService.canUserEditDocument(userId, documentId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to edit this document"));
        }

        String newFileName = (String) requestBody.get("newName");

        boolean fileWasUpdated = documentService.updateDocumentName(userId, documentId, newFileName);

        if (!fileWasUpdated) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while updating the document"));
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("message", "Document name updated successfully");
        response.put("name", newFileName);
        response.put("file_id", documentId);
        response.put("updated_timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{document_id}/delete")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable("document_id") String documentId,
            HttpServletRequest request
    ){
        String userId = (String) request.getAttribute("user_id");

        if (!documentService.canUserEditDocument(userId, documentId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to delete this document"));
        }

        boolean fileWasDeleted = documentService.deleteDocument(documentId);

        if (!fileWasDeleted) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while deleting the document"));
        }

        return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
    }
}
