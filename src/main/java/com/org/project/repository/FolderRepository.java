package com.org.project.repository;

import com.org.project.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, String> {
    Folder findByOrganizationIdAndParentFolderIsNull(String organizationId);
}
