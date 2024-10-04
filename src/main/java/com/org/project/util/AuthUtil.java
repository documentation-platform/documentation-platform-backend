package com.org.project.util;

import java.util.Random;

public class AuthUtil {

    private static final Random RANDOM = new Random();

    /**
     * Generates a random integer.
     * @return A random integer between 0 and 9,999,999 (inclusive).
     */
    public static int generateRandomAuthVersion() {
        return RANDOM.nextInt(10000000);
    }
}
