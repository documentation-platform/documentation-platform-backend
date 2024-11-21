package com.org.project.service;

import com.org.project.model.*;
import com.org.project.repository.FileContentRelationRepository;
import com.org.project.repository.FileRepository;
import com.org.project.repository.OrganizationUserRelationRepository;
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
