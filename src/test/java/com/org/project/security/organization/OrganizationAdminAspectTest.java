package com.org.project.security.organization;

import com.org.project.util.OrganizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrganizationAdminAspectTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OrganizationAdminAspect aspect;

    @Test
    void whenUserHasAdminRole_thenAllowAccess() throws IOException {
        when(request.getAttribute("user_organization_access_id"))
                .thenReturn(OrganizationUtil.ORGANIZATION_ADMIN_ROLE_ID);

        aspect.checkAdminRole();

        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void whenUserDoesNotHaveAdminRole_thenSendForbidden() throws IOException {
        when(request.getAttribute("user_organization_access_id"))
                .thenReturn(999);

        aspect.checkAdminRole();

        verify(response).sendError(
                eq(HttpStatus.FORBIDDEN.value()),
                eq("User does not have admin access level to this organization")
        );
    }
}
