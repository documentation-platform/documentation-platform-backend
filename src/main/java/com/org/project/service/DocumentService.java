package com.org.project.service;

import com.org.project.controller.AuthController;
import com.org.project.model.*;
import com.org.project.repository.FileContentRelationRepository;
import com.org.project.repository.FileRepository;
import com.org.project.repository.OrganizationUserRelationRepository;
import com.org.project.security.organization.OrganizationEditor;
import com.org.project.util.AuthUtil;
import com.org.project.util.OrganizationUtil;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {

    @Autowired
    private OrganizationUserRelationRepository organizationUserRelationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileContentRelationRepository fileContentRelationRepository;

    @Autowired
    private EntityManager entityManager;

    public boolean canUserViewDocument(String userId, String documentId) {
        File file = fileRepository.findById(documentId).orElse(null);

        if (file == null || file.getFolder().getOrganization() == null) {
            return false;
        }

        OrganizationUserRelation organizationUserRelation = organizationUserRelationRepository.findByUserIdAndOrganizationId(
                userId, file.getFolder().getOrganization().getId()
        );

        if (organizationUserRelation == null) {
            return false;
        }

        return true;
    }

    public String getDocumentContent(String documentId) {
        FileContentRelation fileContentRelation = fileContentRelationRepository.findById(documentId).orElse(null);

        if (fileContentRelation == null) {
            // Throw an exception if the file content relation is not found
            throw new RuntimeException("File content not found");
        }

        return fileContentRelation.getTextContent();
    }

    public boolean canUserEditDocument(String userId, String documentId) {
        File file = fileRepository.findById(documentId).orElse(null);

        if (file == null || file.getFolder().getOrganization() == null) {
            return false;
        }

        OrganizationUserRelation organizationUserRelation = organizationUserRelationRepository.findByUserIdAndOrganizationId(
                userId, file.getFolder().getOrganization().getId()
        );

        boolean isUserOrganizationEditor = organizationUserRelation.getAccessId() <= OrganizationUtil.ORGANIZATION_EDITOR_ROLE_ID;

        if (!isUserOrganizationEditor || organizationUserRelation == null) {
            return false;
        }

        return true;
    }

    @Transactional
    public boolean updateDocumentName(String documentId, String newName) {
        File file = fileRepository.findById(documentId).orElse(null);

        if (file == null) {
            throw new RuntimeException("File not found");
        }

        file.setName(newName);
        fileRepository.save(file);
        return true;
    }


    @Transactional
    public boolean updateDocumentContent(String userId, String documentId, String newContent) {
        FileContentRelation fileContentRelation = fileContentRelationRepository.findById(documentId).orElse(null);

        if (fileContentRelation == null) {
            throw new RuntimeException("File content not found");
        }

        fileContentRelation.setTextContent(newContent);
        fileContentRelation.setUpdatedUser(entityManager.getReference(User.class, userId));
        fileContentRelationRepository.save(fileContentRelation);
        return true;
    }

    @Transactional
    public File createOrganizationDocument(String userId, String organizationId) {
        Folder folder = organizationService.getOrganizationRootFolder(organizationId);
        User user = entityManager.getReference(User.class, userId);
        File file = new File();
        file.setFolder(folder);
        file.setCreationUser(user);
        File newFile = fileRepository.save(file);

        FileContentRelation fileContentRelation = new FileContentRelation();
        fileContentRelation.setFileId(newFile.getId());
        fileContentRelation.setFile(newFile);
        fileContentRelation.setUpdatedUser(user);
        fileContentRelation.setTextContent("");
        fileContentRelationRepository.save(fileContentRelation);
        return file;
    }
}
