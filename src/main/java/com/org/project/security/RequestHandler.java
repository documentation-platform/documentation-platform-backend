package com.org.project.security;

import com.org.project.controller.AuthController;
import com.org.project.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestHandler implements HandlerInterceptor {

    private final String accessCookieName = AuthController.ACCESS_TOKEN_COOKIE_NAME;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // HandlerMethod represents the controller method being called
        // Spring gives us this information through the handler parameter
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Secured methodSecured = handlerMethod.getMethodAnnotation(Secured.class);
        Secured classSecured = handlerMethod.getBeanType().getAnnotation(Secured.class);

        if (methodSecured == null && classSecured == null) {
            return true;
        }

        String accessToken = authUtil.getTokenFromCookie(request, accessCookieName);

        if (accessToken != null && authUtil.isAccessTokenValid(accessToken)) {
            request.setAttribute("user_id", authUtil.getUserIdFromAcessToken(accessToken));
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
