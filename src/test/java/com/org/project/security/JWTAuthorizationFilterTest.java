package com.org.project.security;

import com.org.project.controller.AuthController;
import com.org.project.util.AuthUtil;
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
import jakarta.servlet.http.Cookie;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JWTAuthorizationFilterTest {

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private JWTAuthorizationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void whenPublicRoute_thenAllowAccess() throws Exception {
        request.setRequestURI("/api/health");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(authUtil, never()).isAccessTokenValid(any());
        verify(authUtil, never()).getUserIdFromAccessToken(any());
    }

    @Test
    void whenPublicAuthRoute_thenAllowAccess() throws Exception {
        request.setRequestURI("/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(authUtil, never()).isAccessTokenValid(any());
        verify(authUtil, never()).getUserIdFromAccessToken(any());
    }

    @Test
    void whenAuthenticatedExceptionRoute_thenRequireAuth() throws Exception {
        request.setRequestURI("/auth/logout_all");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void whenValidTokenProvided_thenAllowAccess() throws Exception {
        request.setRequestURI("/protected/resource");
        String validToken = "valid.jwt.token";
        String userId = "test-user-123";

        Cookie cookie = new Cookie(AuthController.ACCESS_TOKEN_COOKIE_NAME, validToken);
        request.setCookies(cookie);

        when(authUtil.isAccessTokenValid(validToken)).thenReturn(true);
        when(authUtil.getUserIdFromAccessToken(validToken)).thenReturn(userId);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals(userId, request.getAttribute("user_id"));
        verify(authUtil).isAccessTokenValid(validToken);
        verify(authUtil).getUserIdFromAccessToken(validToken);
    }

    @Test
    void whenInvalidTokenProvided_thenUnauthorized() throws Exception {
        request.setRequestURI("/protected/resource");
        String invalidToken = "invalid.jwt.token";

        Cookie cookie = new Cookie(AuthController.ACCESS_TOKEN_COOKIE_NAME, invalidToken);
        request.setCookies(cookie);

        when(authUtil.isAccessTokenValid(invalidToken)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(authUtil).isAccessTokenValid(invalidToken);
        verify(authUtil, never()).getUserIdFromAccessToken(any());
    }

    @Test
    void whenNoCookieProvided_thenUnauthorized() throws Exception {
        request.setRequestURI("/protected/resource");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(authUtil, never()).isAccessTokenValid(any());
        verify(authUtil, never()).getUserIdFromAccessToken(any());
    }

    @Test
    void whenMultipleCookiesProvided_thenFindCorrectOne() throws Exception {
        request.setRequestURI("/protected/resource");
        String validToken = "valid.jwt.token";
        String userId = "test-user-123";

        Cookie[] cookies = {
                new Cookie("other-cookie", "some-value"),
                new Cookie(AuthController.ACCESS_TOKEN_COOKIE_NAME, validToken),
                new Cookie("another-cookie", "another-value")
        };
        request.setCookies(cookies);

        when(authUtil.isAccessTokenValid(validToken)).thenReturn(true);
        when(authUtil.getUserIdFromAccessToken(validToken)).thenReturn(userId);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals(userId, request.getAttribute("user_id"));
        verify(authUtil).isAccessTokenValid(validToken);
        verify(authUtil).getUserIdFromAccessToken(validToken);
    }

    @Test
    void whenWrongCookieName_thenUnauthorized() throws Exception {
        request.setRequestURI("/protected/resource");
        Cookie cookie = new Cookie("wrong-cookie-name", "valid.jwt.token");
        request.setCookies(cookie);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(authUtil, never()).isAccessTokenValid(any());
        verify(authUtil, never()).getUserIdFromAccessToken(any());
    }
}
