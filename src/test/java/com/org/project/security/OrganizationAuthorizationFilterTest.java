package com.org.project.security;

import com.org.project.model.OrganizationUserRelation;
import com.org.project.service.OrganizationService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrganizationAuthorizationFilterTest {

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private OrganizationAuthorizationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        request.setAttribute("user_id", "test-user-id");
    }

    @Test
    void whenValidRequestAndUserHasAccess_thenSucceed() throws Exception {
        String organizationId = "org123";
        request.setRequestURI("/organization/" + organizationId + "/users");

        OrganizationUserRelation relation = new OrganizationUserRelation();
        relation.setAccessId(1);

        when(organizationService.getUserOrganizationRelation("test-user-id", organizationId))
                .thenReturn(relation);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(1, request.getAttribute("user_organization_access_id"));
        verify(organizationService).getUserOrganizationRelation("test-user-id", organizationId);
    }

    @Test
    void whenUserDoesNotHaveAccess_thenReturn403() throws Exception {
        String organizationId = "org123";
        request.setRequestURI("/organization/" + organizationId + "/users");

        when(organizationService.getUserOrganizationRelation("test-user-id", organizationId))
                .thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void whenInvalidPath_thenSkipFilter() throws Exception {
        request.setRequestURI("/api/health");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(organizationService, never()).getUserOrganizationRelation(any(), any());
    }

    @Test
    void whenInvalidSuffix_thenSkipFilter() throws Exception {
        request.setRequestURI("/organization/org123");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(organizationService, never()).getUserOrganizationRelation(any(), any());
    }

    @Test
    void whenValidPathWithMultipleSegments_thenSucceed() throws Exception {
        String organizationId = "org123";
        request.setRequestURI("/organization/" + organizationId + "/users/settings/profile");

        OrganizationUserRelation relation = new OrganizationUserRelation();
        relation.setAccessId(2);

        when(organizationService.getUserOrganizationRelation("test-user-id", organizationId))
                .thenReturn(relation);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(2, request.getAttribute("user_organization_access_id"));
        assertEquals(200, response.getStatus());
    }
}
