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
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@Component
public class OrganizationAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private OrganizationService organizationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        String organizationPattern = "/organization/{organizationId}/**";
        String documentOrganizationPattern = "/document/organization/{organizationId}/**";
        AntPathMatcher pathMatcher = new AntPathMatcher();

        if ((pathMatcher.match(organizationPattern, requestURI) || pathMatcher.match(documentOrganizationPattern, requestURI))
                && hasValidSuffix(requestURI)) {

            String matchedPattern = pathMatcher.match(organizationPattern, requestURI)
                    ? organizationPattern
                    : documentOrganizationPattern;

            String organizationId = pathMatcher.extractUriTemplateVariables(matchedPattern, requestURI).get("organizationId");

            if (!handleOrganizationRequest(request, response, organizationId)) {
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasValidSuffix(String requestURI) {
        String pattern = "(/organization/[^/]+/.+)|(/document/organization/[^/]+/.+)";
        return requestURI.matches(pattern);
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
