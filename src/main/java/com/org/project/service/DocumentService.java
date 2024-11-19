package com.org.project.service;

import com.org.project.model.OrganizationUserRelation;
import com.org.project.repository.OrganizationUserRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    @Autowired
    private OrganizationUserRelationRepository organizationUserRelationRepository;



    public boolean canUserViewDocument(String userId, String documentId) {



        OrganizationUserRelation relation = organizationUserRelationRepository.findByUserIdAndOrganizationId(userId, documentId);
        return relation != null && relation.getAccessId() == 1;
    }
}
