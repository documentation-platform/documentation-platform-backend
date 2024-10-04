package com.org.project.component;

import com.org.project.model.auth.AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Random;

@Component
public class AuthUtil {
    @Value("${JWT_ACCESS_SECRET}")
    private String jwtAccessSecret;

    @Value("${JWT_ACCESS_EXPIRATION_SECONDS}")
    private Integer jwtAccessExpirationSeconds;

    private static final Random RANDOM = new Random();

    /**
     * Generates a random integer.
     * @return A random integer between 0 and 9,999,999 (inclusive).
     */
    public static int generateRandomAuthVersion() {
        return RANDOM.nextInt(10000000);
    }

    public AccessToken createAccessToken(Integer userId){
        return new AccessToken(userId, jwtAccessSecret, jwtAccessExpirationSeconds);
    }
}

