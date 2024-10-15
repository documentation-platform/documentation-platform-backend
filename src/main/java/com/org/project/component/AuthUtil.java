package com.org.project.component;

import com.org.project.model.auth.AccessToken;
import com.org.project.model.auth.RefreshToken;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Random;

@Component
public class AuthUtil {
    @Value("${JWT_ACCESS_SECRET}")
    private String jwtAccessSecret;

    @Value("${JWT_ACCESS_EXPIRATION_SECONDS}")
    private Integer jwtAccessExpirationSeconds;

    @Value("${JWT_REFRESH_SECRET}")
    private String jwtRefreshSecret;

    @Value("${JWT_REFRESH_EXPIRATION_SECONDS}")
    private Integer jwtRefreshExpirationSeconds;

    private static final Random RANDOM = new Random();

    /**
     * Generates a random integer.
     * @return A random integer between 0 and 9,999,999 (inclusive).
     */
    public static int generateRandomAuthVersion() {
        return RANDOM.nextInt(10000000);
    }

    /**
     * Generates an access token in the form of a JWT
     * @return Access Token as JWT
     */
    public AccessToken createAccessToken(String userId){
        return new AccessToken(userId, jwtAccessSecret, jwtAccessExpirationSeconds);
    }

    /**
     * Generates a refresh token in the form of a JWT
     * @return Refresh Token as JWT
     */
    public RefreshToken createRefreshToken(String userId, Integer authVersion){
        return new RefreshToken(userId, authVersion, jwtRefreshSecret, jwtRefreshExpirationSeconds);
    }

    /**
     * Generates a cookie that includes the token
     * @return Cookie with the token
     */
    public Cookie createTokenCookie(String cookieName, String token, Integer expiration){
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(expiration);

        return cookie;
    }
}
