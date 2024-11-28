package com.org.project.exception;

public class ParentFolderPermissionException extends RuntimeException {
    public ParentFolderPermissionException() {
        super("You do not have permission to access the parent folder");
    }
}
