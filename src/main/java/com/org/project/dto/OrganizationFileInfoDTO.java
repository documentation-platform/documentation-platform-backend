package com.org.project.dto;

import java.util.Date;

public class OrganizationFileInfoDTO {
    private String fileId;
    private String name;
    private String updatedUserId;
    private Date updatedAt;

    public OrganizationFileInfoDTO(String fileId, String name, String updatedUserId, Date updatedAt) {
        this.fileId = fileId;
        this.name = name;
        this.updatedUserId = updatedUserId;
        this.updatedAt = updatedAt;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdatedUserId() {
        return updatedUserId;
    }

    public void setUpdatedUserId(String updatedUserId) {
        this.updatedUserId = updatedUserId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
