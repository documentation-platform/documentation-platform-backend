package com.org.project.util;

import com.org.project.controller.AuthController;
import com.org.project.model.auth.AccessToken;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;


public class AuthUtilTest {

    private AuthUtil authUtil;
    private static final Integer TEST_JWT_EXPIRATION = 3600;
    private static final Integer TEST_JWT_REFRESH_EXPIRATION = 86400;
    private static final String TEST_JWT_SECRET = "divyematsBHqHUxi6QD5D811iWH7qNxUW9U/QboseFw=";

    @BeforeEach
    void setUp() {
        authUtil = new AuthUtil();
        ReflectionTestUtils.setField(authUtil, "jwtAccessExpirationSeconds", TEST_JWT_EXPIRATION);
        ReflectionTestUtils.setField(authUtil, "jwtRefreshExpirationSeconds", TEST_JWT_REFRESH_EXPIRATION);
        ReflectionTestUtils.setField(authUtil, "jwtAccessSecret", TEST_JWT_SECRET);
        ReflectionTestUtils.setField(authUtil, "jwtRefreshSecret", TEST_JWT_SECRET);
    }

    @Test
    public void createTokenCookieTest() {
        String cookieName = "testCookie";
        String token = "testToken";

        Cookie cookie = authUtil.createTokenCookie(cookieName, token);

        Assertions.assertEquals(cookieName, cookie.getName());
        Assertions.assertEquals(token, cookie.getValue());
        Assertions.assertEquals("/", cookie.getPath());
    }

    @Test
    public void createRefreshTokenTest() {
        String cookieName = AuthController.REFRESH_TOKEN_COOKIE_NAME;
        String token = "testToken";

        Cookie cookie = authUtil.createTokenCookie(cookieName, token);

        Assertions.assertEquals(cookieName, cookie.getName());
        Assertions.assertEquals(token, cookie.getValue());
        Assertions.assertEquals("/auth/refresh", cookie.getPath());
    }

    @Test
    public void testValidAccessToken() {
        String userId = "testId";
        AccessToken accessToken = authUtil.createAccessToken(userId);
        String token = accessToken.token;

        Assertions.assertTrue(authUtil.isAccessTokenValid(token));
    }

    @Test
    public void testInvalidAccessToken() {
        String token = "invalidToken";
        Assertions.assertFalse(authUtil.isAccessTokenValid(token));
    }

    @Test
    public void testValidRefreshToken() {
        String userId = "testId";
        Integer authVersion = 1;
        String token = authUtil.createRefreshToken(userId, authVersion).token;

        Assertions.assertTrue(authUtil.isRefreshTokenAuthVersionValid(token, authVersion));
    }

    @Test
    public void testInvalidRefreshToken() {
        String userId = "testId";
        Integer authVersion = 1;
        String token = authUtil.createRefreshToken(userId, authVersion).token;

        Assertions.assertFalse(authUtil.isRefreshTokenAuthVersionValid(token, authVersion + 1));
    }
}