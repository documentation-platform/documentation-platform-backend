package com.org.project.service;

import com.org.project.dto.OrganizationFileInfoDTO;
import com.org.project.dto.UserFileInfoDTO;
import com.org.project.model.*;
import com.org.project.repository.FileContentRelationRepository;
import com.org.project.repository.FileRepository;
import com.org.project.repository.FolderRepository;
import com.org.project.repository.OrganizationUserRelationRepository;
import com.org.project.util.OrganizationUtil;
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
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

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
    public File createOrganizationDocument(String userId, String organizationId) {
        Folder folder = organizationService.getOrganizationRootFolder(organizationId);
        User user = entityManager.getReference(User.class, userId);
        File file = new File();
        file.setFolder(folder);
        file.setCreationUser(user);
        file.setUpdatedUser(user);
        File newFile = fileRepository.save(file);

        FileContentRelation fileContentRelation = new FileContentRelation();
        fileContentRelation.setFileId(newFile.getId());
        fileContentRelation.setFile(newFile);
        fileContentRelation.setTextContent("");
        fileContentRelationRepository.save(fileContentRelation);
        return file;
    }

    @Transactional
    public File createUserDocument(String userId) {
        Folder folder = getRootUserFolder(userId);
        User user = entityManager.getReference(User.class, userId);
        File file = new File();
        file.setFolder(folder);
        file.setCreationUser(user);
        file.setUpdatedUser(user);
        File newFile = fileRepository.save(file);

        FileContentRelation fileContentRelation = new FileContentRelation();
        fileContentRelation.setFileId(newFile.getId());
        fileContentRelation.setFile(newFile);
        fileContentRelation.setTextContent("");
        fileContentRelationRepository.save(fileContentRelation);
        return file;
    }

    private Folder getRootUserFolder(String userId) {
        Folder rootFolder = folderRepository.findByUserIdAndParentFolderIsNull(userId);

        if (rootFolder == null) {
            User user = entityManager.getReference(User.class, userId);
            rootFolder = new Folder();
            rootFolder.setName("root");
            rootFolder.setUser(user);
            folderRepository.save(rootFolder);
        }

        return rootFolder;
    }
}
