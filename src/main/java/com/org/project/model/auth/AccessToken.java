package com.org.project.model.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.util.Date;

public class AccessToken {
    public String token;

    public AccessToken(Integer userId, String jwtAccessSecret, Integer jwtAccessExpirationSeconds) {
        Date issuedDate = Date.from(Instant.now());
        Date expiryDate = Date.from(Instant.now().plusSeconds(jwtAccessExpirationSeconds));

        this.token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(issuedDate)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtAccessSecret)
                .compact();
    }
}