package com.org.project.dto.structure;

import com.org.project.model.File;
import com.org.project.model.Folder;

import java.util.List;

public class FolderStructureDTO {
    private String structureFolderId;
    private List<FolderFileInfoDTO> folders;
    private List<FolderFileInfoDTO> files;

    public FolderStructureDTO(String structureFolderId, List<FolderFileInfoDTO> folders, List<FolderFileInfoDTO> files) {
        this.structureFolderId = structureFolderId;
        this.folders = folders;
        this.files = files;
    }

    public String getStructureFolderId() {
        return structureFolderId;
    }

    public List<FolderFileInfoDTO> getFolders() {
        return folders;
    }

    public List<FolderFileInfoDTO> getFiles() {
        return files;
    }
}
