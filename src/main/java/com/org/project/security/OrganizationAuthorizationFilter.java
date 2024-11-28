package com.org.project.security;

import com.org.project.service.OrganizationService;
import com.org.project.model.OrganizationUserRelation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OrganizationAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private OrganizationService organizationService;

    private static final Pattern ORGANIZATION_URL_PATTERN = Pattern.compile("^(.*/)?organization/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(/.*)?$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        Matcher matcher = ORGANIZATION_URL_PATTERN.matcher(requestURI);

        if (matcher.matches()) {
            String organizationId = matcher.group(2);

            if (!handleOrganizationRequest(request, response, organizationId)) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean handleOrganizationRequest(HttpServletRequest request, HttpServletResponse response, String organizationId) throws IOException {
        String userId = (String) request.getAttribute("user_id");

        if (organizationId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing organization_id in request path");
            return false;
        }

        OrganizationUserRelation userOrganizationRelation =
                organizationService.getUserOrganizationRelation(userId, organizationId);

        if (userOrganizationRelation == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User does not have access to this organization");
            return false;
        }

        request.setAttribute("user_organization_access_id", userOrganizationRelation.getAccessId());
        return true;
    }
}
