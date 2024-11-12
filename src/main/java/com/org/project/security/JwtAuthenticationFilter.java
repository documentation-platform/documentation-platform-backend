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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthUtil authUtil;

    private static final List<String> PUBLIC_ROUTES = List.of("/health", "/auth/**");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isPublicRoute(request.getRequestURI())) {
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
        return PUBLIC_ROUTES.stream().anyMatch(route -> requestURI.startsWith(route.replace("**", "")));
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
