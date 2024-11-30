package com.org.project.dto.structure;

import java.util.Date;

public class FolderFileInfoDTO {
    private final String id;
    private final String name;
    private final Date updatedAt;

    public FolderFileInfoDTO(String id, String name, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
