package com.org.project.repository;

import com.org.project.model.Invite;
import com.org.project.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteRepository extends JpaRepository<Invite, String> {

    List<Invite> findByOrganization(Organization organization);
}
