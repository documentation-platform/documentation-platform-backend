package com.org.project.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hashes a password using BCrypt.
     * @return The provided password hashed.
     */
    public static String hashPassword(String password) {
        return encoder.encode(password);
    }

    /**
     * Verifies the given password with the hashed password.
     * @return The boolean stating the match status.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
