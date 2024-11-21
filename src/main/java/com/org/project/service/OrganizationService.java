package com.org.project.service;

import com.org.project.exception.AccountExistsException;
import com.org.project.model.Folder;
import com.org.project.model.Organization;
import com.org.project.model.OrganizationUserRelation;
import com.org.project.model.User;
import com.org.project.dto.RegisterRequestDTO;
import com.org.project.repository.FolderRepository;
import com.org.project.repository.OrganizationRepository;
import com.org.project.repository.OrganizationUserRelationRepository;
import com.org.project.repository.UserRepository;

import com.org.project.util.AuthUtil;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationUserRelationRepository organizationUserRelationRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private EntityManager entityManager;

    public OrganizationUserRelation getUserOrganizationRelation(String userId, String organizationId) {
        return organizationUserRelationRepository.findByUserIdAndOrganizationId(userId, organizationId);
    }

    @Transactional
    public Folder createOrganizationRootFolder(String organizationId) {
        Organization organization = entityManager.getReference(Organization.class, organizationId);
        Folder folder = new Folder();
        folder.setName("root");
        folder.setOrganization(organization);
        folderRepository.save(folder);
        return folder;
    }

    public Folder getOrganizationRootFolder(String organizationId) {
        return folderRepository.findByOrganizationIdAndParentFolderIsNull(organizationId);
    }
}
