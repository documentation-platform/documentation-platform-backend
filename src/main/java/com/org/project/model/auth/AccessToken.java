package com.org.project.model.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.util.Date;

public class AccessToken {
    public String token;
    public Integer expiration;

    public AccessToken(String userId, String jwtAccessSecret, Integer jwtAccessExpirationSeconds) {
        Date issuedDate = Date.from(Instant.now());
        Date expiryDate = Date.from(Instant.now().plusSeconds(jwtAccessExpirationSeconds));

        Claims claims = Jwts.claims();
        claims.put("user_id", userId);

        this.expiration = jwtAccessExpirationSeconds;
        this.token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedDate)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtAccessSecret)
                .compact();
    }
}