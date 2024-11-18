package com.org.project.security.organization;

import com.org.project.util.OrganizationUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@Aspect
@Component
public class OrganizationAdminAspect {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Before("@annotation(OrganizationAdmin)")
    public void checkAdminRole() throws IOException {
        Integer organization_access_id = (Integer) request.getAttribute("user_organization_access_id");

        if (!organization_access_id.equals(OrganizationUtil.ORGANIZATION_ADMIN_ROLE_ID)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "User does not have admin access level to this organization");
        }
    }
}
