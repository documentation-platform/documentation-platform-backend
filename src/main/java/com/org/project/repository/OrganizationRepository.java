package com.org.project.repository;

import com.org.project.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    // This method is inherited from JpaRepository
    Optional<Organization> findFirstByOrderByIdAsc();
}