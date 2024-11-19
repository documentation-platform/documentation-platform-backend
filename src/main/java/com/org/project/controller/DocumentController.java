package com.org.project.controller;

import com.org.project.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @RequestMapping
    @GetMapping("{document_id}/view")
    public ResponseEntity<Map<String, Object>> viewDocument(
            @PathVariable("document_id") String documentId,
            HttpServletRequest request
    ){
        String userId = (String) request.getAttribute("user_id");

        if (!documentService.canUserViewDocument(userId, documentId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(Map.of("document_id", documentId));
    }
}
