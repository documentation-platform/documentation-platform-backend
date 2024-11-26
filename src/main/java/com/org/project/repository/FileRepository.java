package com.org.project.repository;

import com.org.project.dto.OrganizationFileInfoDTO;
import com.org.project.dto.UserFileInfoDTO;
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
    SELECT new com.org.project.dto.OrganizationFileInfoDTO(f.id, f.name, fd.name, f.updatedUser.name, f.updatedAt)
    FROM File f
    JOIN Folder fd ON f.folder.id = fd.id
    WHERE (f.updatedUser.id = :userId AND fd.organization.id = :organizationId)
    ORDER BY f.updatedAt DESC
    """)
    Page<OrganizationFileInfoDTO> findTop5FileInfoByUserIdAndOrganizationId(@Param("userId") String userId,
                                                                            @Param("organizationId") String organizationId,
                                                                            Pageable pageable);

    @Query("""
    SELECT new com.org.project.dto.UserFileInfoDTO(f.id, f.name, fd.name, f.updatedAt)
    FROM File f
    JOIN Folder fd ON f.folder.id = fd.id
    WHERE (f.updatedUser.id = :userId AND fd.user.id = :userId)
    ORDER BY f.updatedAt DESC
    """)
    Page<UserFileInfoDTO> findTop5FileInfoByUserId(@Param("userId") String userId,
                                                   Pageable pageable);
}
