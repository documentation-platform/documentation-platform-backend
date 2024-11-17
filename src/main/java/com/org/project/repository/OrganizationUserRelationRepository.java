package com.org.project.repository;

import com.org.project.model.OrganizationUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationUserRelationRepository extends JpaRepository<OrganizationUserRelation, Long> {
    OrganizationUserRelation findByUserIdAndOrganizationId(String userId, String organizationId);
    List<OrganizationUserRelation> findByUserId(String userId);
    List<OrganizationUserRelation> findByOrganizationId(String organizationId);
}
