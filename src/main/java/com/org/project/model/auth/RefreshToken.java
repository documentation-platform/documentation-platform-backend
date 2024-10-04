package com.org.project.model.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.util.Date;

public class RefreshToken {
    public String token;
    public Integer expiration;

    public RefreshToken(Integer userId, Integer authVersion, String jwtRefreshSecret, Integer jwtRefreshExpirationSeconds) {
        Date issuedDate = Date.from(Instant.now());
        Date expiryDate = Date.from(Instant.now().plusSeconds(jwtRefreshExpirationSeconds));

        Claims claims = Jwts.claims();
        claims.put("user_id", userId);
        claims.put("auth_version", authVersion);

        this.expiration = jwtRefreshExpirationSeconds;
        this.token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedDate)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtRefreshSecret)
                .compact();
    }
}