package com.org.project.security;

import com.org.project.controller.AuthController;
import com.org.project.util.AuthUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthUtil authUtil;

    private static final List<String> PUBLIC_ROUTES = List.of("/api/health");
    private static final List<String> PUBLIC_AUTH_ROUTES = List.of("/auth");
    private static final List<String> AUTHENTICATED_EXCEPTIONS = List.of("/auth/logout_all");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (isPublicRoute(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessCookieName = AuthController.ACCESS_TOKEN_COOKIE_NAME;
        String accessToken = getTokenFromCookie(request, accessCookieName);

        if (accessToken != null && authUtil.isAccessTokenValid(accessToken)) {
            String userId = authUtil.getUserIdFromAccessToken(accessToken);
            request.setAttribute("user_id", userId);
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean isPublicRoute(String requestURI) {
        if (PUBLIC_ROUTES.contains(requestURI)) {
            return true;
        }

        if (PUBLIC_AUTH_ROUTES.stream().anyMatch(requestURI::startsWith) && !AUTHENTICATED_EXCEPTIONS.contains(requestURI)) {
            return true;
        }

        return false;
    }

    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
