package com.org.project;

import com.org.project.util.AuthUtil;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestUtils {
    public static RequestPostProcessor withAuth() {
        return request -> {
            request.setAttribute("user_id", "test_user_id");
            request.setCookies(new Cookie("JWT_Access_Token", "valid_test_token"));
            return request;
        };
    }

    public static void setupAuthMocks(AuthUtil authUtil) {
        when(authUtil.getTokenFromCookie(any(), any())).thenReturn("valid_test_token");
        when(authUtil.isAccessTokenValid(any())).thenReturn(true);
        when(authUtil.getUserIdFromAcessToken(any())).thenReturn("test_user_id");
    }
}
