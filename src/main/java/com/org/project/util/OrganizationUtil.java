package com.org.project.util;

import org.springframework.stereotype.Component;

@Component
public class OrganizationUtil {
    public static final Integer ORGANIZATION_ADMIN_ROLE_ID = 1;
    public static final Integer ORGANIZATION_EDITOR_ROLE_ID = 2;
    public static final Integer ORGANIZATION_VIEWER_ROLE_ID = 3;

    public static Object getAccessLevel(Integer accessId) {
        switch (accessId) {
            case 1:
                return "Admin";
            case 2:
                return "Editor";
            case 3:
                return "Viewer";
            default:
                return "Unknown";
        }
    }
}
