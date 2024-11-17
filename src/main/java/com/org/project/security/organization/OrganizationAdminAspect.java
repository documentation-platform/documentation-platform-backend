package com.org.project.security.organization;

import com.org.project.util.OrganizationUtil;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class OrganizationAdminAspect {

    @Autowired
    private HttpServletRequest request;

    @Before("@annotation(OrganizationAdmin)")
    public void checkAdminRole() {
        Integer organization_access_id = (Integer) request.getAttribute("user_organization_access_id");

        if (!organization_access_id.equals(OrganizationUtil.ORGANIZATION_ADMIN_ROLE_ID)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User does not have admin access to this organization"
            );
        }
    }
}
