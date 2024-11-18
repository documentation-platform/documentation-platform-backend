package com.org.project.security.organization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationEditorAspectTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OrganizationEditorAspect aspect;

    @Test
    void whenUserHasEditorAccess_thenAllowAccess() throws IOException {
        when(request.getAttribute("user_organization_access_id"))
                .thenReturn(2);

        aspect.checkEditorRole();

        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void whenUserHasAdminAccess_thenAllowAccess() throws IOException {
        when(request.getAttribute("user_organization_access_id"))
                .thenReturn(1);

        aspect.checkEditorRole();

        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void whenUserDoesNotHaveEditorAccess_thenSendForbidden() throws IOException {
        when(request.getAttribute("user_organization_access_id"))
                .thenReturn(999);

        aspect.checkEditorRole();

        verify(response).sendError(
                eq(HttpStatus.FORBIDDEN.value()),
                eq("User does not have editor access level to this organization")
        );
    }

    @Test
    void whenUserDoesNotHaveEditorAccess2_thenSendForbidden() throws IOException {
        when(request.getAttribute("user_organization_access_id"))
                .thenReturn(-1);

        aspect.checkEditorRole();

        verify(response).sendError(
                eq(HttpStatus.FORBIDDEN.value()),
                eq("User does not have editor access level to this organization")
        );
    }
}
