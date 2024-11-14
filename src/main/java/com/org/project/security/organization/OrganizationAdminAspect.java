package com.org.project.security.organization;

import com.org.project.service.UserService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class OrganizationAdminAspect {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Before("@annotation(OrganizationAdmin)")
    public void checkAdminRole() {
        String user_id = (String) request.getAttribute("user_id");
        Integer organizationId = Integer.valueOf(request.getParameter("organizationId"));
        boolean isAdmin = userService.isUserOrganizationAdmin(user_id, organizationId);

        if (!isAdmin) {
            throw new AccessDeniedException("You do not have admin privileges for this organization.");
        }
    }
}
