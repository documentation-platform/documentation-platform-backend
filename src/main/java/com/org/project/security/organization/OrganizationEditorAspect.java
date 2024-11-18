package com.org.project.security.organization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.org.project.util.OrganizationUtil.ORGANIZATION_ADMIN_ROLE_ID;
import static com.org.project.util.OrganizationUtil.ORGANIZATION_EDITOR_ROLE_ID;

@Aspect
@Component
public class OrganizationEditorAspect {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Before("@annotation(OrganizationEditor)")
    public void checkEditorRole() throws IOException {
        Integer organization_access_id = (Integer) request.getAttribute("user_organization_access_id");

        if (!hasEditorAccess(organization_access_id)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "User does not have editor access level to this organization");
        }
    }

    public static boolean hasEditorAccess(int accessId) {
        return accessId >= ORGANIZATION_ADMIN_ROLE_ID && accessId <= ORGANIZATION_EDITOR_ROLE_ID;
    }
}
