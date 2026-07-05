package com.example.gymcrm.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.Predicate;

@Component
public class UsernamePasswordGenerator {

    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName, Predicate<String> existsByUsername) {
        String base = firstName.trim() + "." + lastName.trim();
        String candidate = base;
        int suffix = 1;
        while (existsByUsername.test(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    public String generatePassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
