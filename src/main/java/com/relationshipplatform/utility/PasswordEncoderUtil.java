package com.relationshipplatform.utility;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Utility class for password encoding and validation
 * Uses BCrypt for secure password hashing
 */
@Component
public class PasswordEncoderUtil {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncoderUtil() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Encode raw password
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verify if raw password matches encoded password
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}