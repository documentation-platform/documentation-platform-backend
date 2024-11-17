package com.org.project.service;

import com.org.project.exception.AccountExistsException;
import com.org.project.model.OrganizationUserRelation;
import com.org.project.model.User;
import com.org.project.dto.RegisterRequestDTO;
import com.org.project.repository.OrganizationRepository;
import com.org.project.repository.OrganizationUserRelationRepository;
import com.org.project.repository.UserRepository;

import com.org.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationUserRelationRepository organizationUserRelationRepository;

    public OrganizationUserRelation getUserOrganizationRelation(String userId, String organizationId) {
        return organizationUserRelationRepository.findByUserIdAndOrganizationId(userId, organizationId);
    }
}
