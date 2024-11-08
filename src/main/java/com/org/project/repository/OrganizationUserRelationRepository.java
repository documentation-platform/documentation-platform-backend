package com.org.project.repository;

import com.org.project.model.OrganizationUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.org.project.model.OrganizationUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationUserRelationRepository extends JpaRepository<OrganizationUserRelation, Long> {
    OrganizationUserRelation findByUserIdAndOrganizationId(String userId, Integer organizationId);
}
