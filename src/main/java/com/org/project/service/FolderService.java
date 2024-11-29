package com.org.project.service;

import com.org.project.exception.ParentFolderPermissionException;
import com.org.project.model.Folder;
import com.org.project.model.Organization;
import com.org.project.model.User;
import com.org.project.repository.FolderRepository;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FolderService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FolderRepository folderRepository;

    public Folder createUserFolder(String userId, String folderName, @Nullable String parentFolderId) {
        if (parentFolderId != null && !canUserAccessFolder(userId, parentFolderId)) {
            throw new ParentFolderPermissionException();
        }

        Folder newFolder = new Folder();
        newFolder.setName(folderName);
        newFolder.setUser(entityManager.getReference(User.class, userId));
        if (parentFolderId != null) {
            Folder parentFolder = entityManager.getReference(Folder.class, parentFolderId);
            newFolder.setParentFolder(parentFolder);
        } else {
            newFolder.setParentFolder(getRootUserFolder(userId));
        }

        return folderRepository.save(newFolder);
    }

    public Folder createOrganizationFolder(String organizationId, String folderName, @Nullable String parentFolderId) {
        if (parentFolderId != null && !canOrganizationAccessFolder(organizationId, parentFolderId)) {
            throw new ParentFolderPermissionException();
        }

        Folder newFolder = new Folder();
        newFolder.setName(folderName);
        newFolder.setOrganization(entityManager.getReference(Organization.class, organizationId));
        if (parentFolderId != null) {
            Folder parentFolder = entityManager.getReference(Folder.class, parentFolderId);
            newFolder.setParentFolder(parentFolder);
        } else {
            newFolder.setParentFolder(getRootOrganizationFolder(organizationId));
        }

        return folderRepository.save(newFolder);
    }

    public Folder moveUserFolder(String userId, String folderId, String parentFolderId) {
        if (!canUserAccessFolder(userId, folderId) || !canUserAccessFolder(userId, parentFolderId)) {
            throw new ParentFolderPermissionException();
        }

        Folder folder = entityManager.getReference(Folder.class, folderId);
        Folder parentFolder = entityManager.getReference(Folder.class, parentFolderId);
        folder.setParentFolder(parentFolder);

        return folderRepository.save(folder);
    }

    public Folder getRootUserFolder(String userId) {
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

    public Folder getRootOrganizationFolder(String organizationId) {
        Folder rootFolder = folderRepository.findByOrganizationIdAndParentFolderIsNull(organizationId);

        if (rootFolder == null) {
            Organization organization = entityManager.getReference(Organization.class, organizationId);
            rootFolder = new Folder();
            rootFolder.setName("root");
            rootFolder.setOrganization(organization);
            folderRepository.save(rootFolder);
        }

        return rootFolder;
    }

    public Boolean canUserAccessFolder(String userId, String folderId) {
        Folder folder = folderRepository.findById(folderId).orElse(null);
        if (folder == null) {
            return false;
        }
        return folder.getUser().getId().equals(userId);
    }

    public Boolean canOrganizationAccessFolder(String organizationId, String folderId) {
        Folder folder = folderRepository.findById(folderId).orElse(null);
        if (folder == null) {
            return false;
        }
        return folder.getOrganization().getId().equals(organizationId);
    }
}
