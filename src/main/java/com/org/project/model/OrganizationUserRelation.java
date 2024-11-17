package com.org.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_user_relation")
public class OrganizationUserRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Auto-generated ID for the relation entry

    @Column(name = "user_id", nullable = false)
    private String userId;  // Foreign key referencing User's id (String)

    @Column(name = "organization_id", nullable = false)
    private String organizationId;  // Foreign key referencing Organization's id (Integer)

    @Column(name = "access_id", nullable = false)
    private Integer accessId;  // Foreign key referencing Access's id (Integer)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // Timestamp for when the relation was created

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();  // Timestamp for when the relation was last updated


    // Default constructor
    public OrganizationUserRelation() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getAccessId() {
        return accessId;
    }

    public void setAccessId(Integer accessId) {
        this.accessId = accessId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Optionally, use @PreUpdate to automatically update 'updated_at' on every update
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}