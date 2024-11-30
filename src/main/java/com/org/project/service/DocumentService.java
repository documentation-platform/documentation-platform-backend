package com.org.project.service;

import com.org.project.dto.OrganizationFileInfoDTO;
import com.org.project.dto.UserFileInfoDTO;
import com.org.project.exception.ParentFolderPermissionException;
import com.org.project.model.*;
import com.org.project.repository.FileContentRelationRepository;
import com.org.project.repository.FileRepository;
import com.org.project.repository.FolderRepository;
import com.org.project.repository.OrganizationUserRelationRepository;
import com.org.project.util.OrganizationUtil;
import com.org.project.service.FolderService;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

@Service
public class DocumentService {

    @Autowired
    private OrganizationUserRelationRepository organizationUserRelationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileContentRelationRepository fileContentRelationRepository;

    @Autowired
    private EntityManager entityManager;

    public boolean canUserViewDocument(String userId, String documentId) {
        File file = fileRepository.findById(documentId).orElse(null);

        if (file == null) {
            return false;
        }

        Folder parentFolder = file.getFolder();

        if (parentFolder != null && parentFolder.getOrganization() != null) {
            return canUserViewOrganizationDocument(userId, parentFolder.getOrganization().getId());
        }

        return parentFolder.getUser().getId().equals(userId);
    }

    public boolean canUserViewOrganizationDocument(String userId, String organizationId) {
        OrganizationUserRelation organizationUserRelation = organizationUserRelationRepository.findByUserIdAndOrganizationId(
                userId, organizationId
        );

        return organizationUserRelation != null;
    }

    public String getDocumentContent(String documentId) {
        FileContentRelation fileContentRelation = fileContentRelationRepository.findById(documentId).orElse(null);

        if (fileContentRelation == null) {
            throw new RuntimeException("File content not found");
        }

        return fileContentRelation.getTextContent();
    }

    public String getDocumentName(String documentId) {
        File file = fileRepository.findById(documentId).orElse(null);

        if (file == null) {
            throw new RuntimeException("File not found");
        }

        return file.getName();
    }

    public boolean canUserEditDocument(String userId, String documentId) {
        File file = fileRepository.findById(documentId).orElse(null);

        if (file == null) {
            return false;
        }

        Folder parentFolder = file.getFolder();

        if (parentFolder == null) {
            return false;
        }

        if (parentFolder.getOrganization() != null) {
            return canUserEditOrganizationDocument(userId, parentFolder.getOrganization().getId());
        }

        return parentFolder.getUser().getId().equals(userId);
    }

    public boolean canUserEditOrganizationDocument(String userId, String organizationId) {
        OrganizationUserRelation organizationUserRelation = organizationUserRelationRepository.findByUserIdAndOrganizationId(
                userId, organizationId
        );

        if (organizationUserRelation == null) {
            return false;
        }

        return organizationUserRelation.getAccessId() <= OrganizationUtil.ORGANIZATION_EDITOR_ROLE_ID;
    }

    public Page<OrganizationFileInfoDTO> getUserRecentOrganizationDocuments(String userId, String organizationId, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return fileRepository.findTop5FileInfoByUserIdAndOrganizationId(userId, organizationId, pageable);
    }

    public Page<UserFileInfoDTO> getUserRecentDocuments(String userId, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return fileRepository.findTop5FileInfoByUserId(userId, pageable);
    }

    @Transactional
    public boolean updateDocumentName(String userId, String documentId, String newName) {
        File file = fileRepository.findById(documentId).orElse(null);

        if (file == null) {
            return false;
        }

        file.setName(newName);
        file.setUpdatedUser(entityManager.getReference(User.class, userId));
        fileRepository.save(file);
        return true;
    }

    @Transactional
    public boolean deleteDocument(String documentId) {
        // TODO: Add history record for deletion

        FileContentRelation fileContentRelation = fileContentRelationRepository.findById(documentId).orElse(null);
        File file = fileRepository.findById(documentId).orElse(null);

        if (file == null || fileContentRelation == null) {
            return false;
        }

        fileContentRelationRepository.delete(fileContentRelation);
        fileRepository.delete(file);
        return true;
    }

    @Transactional
    public boolean updateDocumentContent(String userId, String documentId, String newContent) {
        FileContentRelation fileContentRelation = fileContentRelationRepository.findById(documentId).orElse(null);

        if (fileContentRelation == null) {
            throw new RuntimeException("File content not found");
        }

        fileContentRelation.setTextContent(newContent);
        fileContentRelationRepository.save(fileContentRelation);

        fileRepository.findById(documentId).ifPresent(file -> {
            file.setUpdatedUser(entityManager.getReference(User.class, userId));
            fileRepository.save(file);
        });

        return true;
    }

    @Transactional
    public File createOrganizationDocument(String userId, String organizationId, @Nullable String documentName, @Nullable String parentFolderId) {
        if (parentFolderId != null && !folderService.canOrganizationAccessFolder(organizationId, parentFolderId)) {
            throw new ParentFolderPermissionException();
        }

        Folder parentDocumentFolder = (parentFolderId != null)
                ? entityManager.getReference(Folder.class, parentFolderId)
                : folderService.getRootOrganizationFolder(organizationId);

        User user = entityManager.getReference(User.class, userId);

        File file = new File();
        if (documentName != null && !documentName.isEmpty()) {
            file.setName(documentName);
        }
        file.setFolder(parentDocumentFolder);
        file.setCreationUser(user);
        file.setUpdatedUser(user);

        File newFile = fileRepository.save(file);

        FileContentRelation fileContentRelation = new FileContentRelation();
        fileContentRelation.setFileId(newFile.getId());
        fileContentRelation.setFile(newFile);
        fileContentRelation.setTextContent("");

        fileContentRelationRepository.save(fileContentRelation);

        return newFile;
    }

    @Transactional
    public File createUserDocument(String userId, @Nullable String documentName, @Nullable String parentFolderId) {
        if (parentFolderId != null && !folderService.canUserAccessFolder(userId, parentFolderId)) {
            throw new ParentFolderPermissionException();
        }

        Folder parentDocumentFolder = (parentFolderId != null)
                ? entityManager.getReference(Folder.class, parentFolderId)
                : folderService.getRootUserFolder(userId);

        User user = entityManager.getReference(User.class, userId);

        File file = new File();
        if (documentName != null && !documentName.isEmpty()) {
            file.setName(documentName);
        }
        file.setFolder(parentDocumentFolder);
        file.setCreationUser(user);
        file.setUpdatedUser(user);

        File newFile = fileRepository.save(file);

        FileContentRelation fileContentRelation = new FileContentRelation();
        fileContentRelation.setFileId(newFile.getId());
        fileContentRelation.setFile(newFile);
        fileContentRelation.setTextContent("");

        fileContentRelationRepository.save(fileContentRelation);

        return newFile;
    }
}
