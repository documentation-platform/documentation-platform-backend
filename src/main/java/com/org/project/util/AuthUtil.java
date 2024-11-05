package com.org.project.util;

import com.org.project.controller.AuthController;
import com.org.project.model.auth.AccessToken;
import com.org.project.model.auth.RefreshToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import java.util.Date;

import java.util.List;
import java.util.Objects;
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

    @Value("${JWT_COOKIE_SAME_SITE}")
    private String jwtCookieSameSite;

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
    public Cookie createTokenCookie(String cookieName, String token){
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", jwtCookieSameSite == null ? "Lax" : jwtCookieSameSite);

        // This is just so the cookie doesn't expire when the browser is closed
        // The JWT token will still expire based on the expiration time in the token
        cookie.setMaxAge(31 * 24 * 60 * 60);

        if (Objects.equals(cookieName, AuthController.REFRESH_TOKEN_COOKIE_NAME)) {
            cookie.setPath("/auth/refresh");
        } else {
            cookie.setPath("/");
        }

        return cookie;
    }

    /**
     * Checks if an access token is valid or not
     * @return Boolean
     */
    public Boolean isAccessTokenValid(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtAccessSecret)
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a refresh token auth version is valid or not
     * @return Boolean
     */
    public Boolean isRefreshTokenAuthVersionValid(String token, Integer authVersion) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtRefreshSecret)
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();

            if (!Objects.equals(claims.get("auth_version", Integer.class), authVersion)){
                return false;
            }

            return expiration.after(new Date());
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a refresh token is valid or not
     * @return Boolean
     */
    public Boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtRefreshSecret)
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a request is authorized or not and if the user was previously authorized
     * @return Map of Boolean values (isAuthorized, previouslyAuthorized)
     */
    public List<Boolean> isRequestAuthorized(HttpServletRequest request, String accessCookieName) {
        String accessToken = getTokenFromCookie(request, AuthController.ACCESS_TOKEN_COOKIE_NAME);
        Boolean isAuthorized = (accessToken != null && isAccessTokenValid(accessToken));
        Boolean previouslyAuthorized = (accessToken != null);

        return List.of(isAuthorized, previouslyAuthorized);
    }

    /**
     * Gets token from a cookie based on the cookie name passed.
     * @return Cookie
     */
    public String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Get UserId from access token
     * @return UserId
     */
    public String getUserIdFromAcessToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtAccessSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("user_id", String.class);
    }

    /**
     * Get UserId from refresh token
     * @return UserId
     */
    public String getUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtRefreshSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("user_id", String.class);
    }
}
