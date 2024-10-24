package com.org.project.config;

import com.org.project.controller.AuthController;
import com.org.project.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestHandler implements HandlerInterceptor {

    private final String accessCookieName = AuthController.ACCESS_TOKEN_COOKIE_NAME;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String accessToken = authUtil.getTokenFromCookie(request, accessCookieName);

        if (accessToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (authUtil.isAccessTokenValid(accessToken)) {
            request.setAttribute("user_id", authUtil.getUserIdFromToken(accessToken));
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
