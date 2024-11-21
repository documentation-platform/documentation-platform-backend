package com.org.project.repository;

import com.org.project.model.FileContentRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileContentRelationRepository extends JpaRepository<FileContentRelation, Integer> {
}
