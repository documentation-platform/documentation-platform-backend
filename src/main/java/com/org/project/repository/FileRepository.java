package com.org.project.repository;

import com.org.project.dto.FileInfoDTO;
import com.org.project.model.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FileRepository extends JpaRepository<File, String> {
    @Query("""
    SELECT new com.org.project.dto.FileInfoDTO(f.id, f.name, f.updatedUser.id, f.updatedAt)
    FROM File f
    JOIN Folder fd ON f.folder.id = fd.id
    WHERE (f.updatedUser.id = :userId AND fd.organization.id = :organizationId)
    ORDER BY f.updatedAt DESC
    """)
    Page<FileInfoDTO> findTop5FileInfoByUserIdAndOrganizationId(@Param("userId") String userId,
                                                                @Param("organizationId") String organizationId,
                                                                Pageable pageable);
}
