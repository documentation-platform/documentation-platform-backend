package com.org.project.service;

import com.org.project.model.Folder;
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
}
