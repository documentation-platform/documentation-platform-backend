package com.org.project.repository;

import com.org.project.dto.structure.FolderFileInfoDTO;
import com.org.project.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, String> {
    Folder findByOrganizationIdAndParentFolderIsNull(String organizationId);
    Folder findByUserIdAndParentFolderIsNull(String userId);

    @Query("SELECT new com.org.project.dto.structure.FolderFileInfoDTO(f.id, f.name, f.updatedAt) " +
            "FROM Folder f WHERE f.parentFolder.id = :parentFolderId")
    List<FolderFileInfoDTO> findFoldersByParentFolderId(String parentFolderId);
}
