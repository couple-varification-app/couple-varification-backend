package com.relationshipplatform.utility;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Utility class to generate unique IDs for various entities
 * Format: PREFIX-TIMESTAMP-RANDOM
 */
public class IdGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Generate User ID
     * Format: USR-{timestamp}-{random}
     * Example: USR-1234567890-A3X9K
     */
    public static String generateUserId() {
        return generateId("USR");
    }

    /**
     * Generate Couple ID
     * Format: CPL-{timestamp}-{random}
     * Example: CPL-1234567890-B7Y4M
     */
    public static String generateCoupleId() {
        return generateId("CPL");
    }

    /**
     * Generate Promise ID
     * Format: PRM-{timestamp}-{random}
     * Example: PRM-1234567890-C2Z8N
     */
    public static String generatePromiseId() {
        return generateId("PRM");
    }

    /**
     * Generate Dream ID
     * Format: DRM-{timestamp}-{random}
     * Example: DRM-1234567890-D5W3P
     */
    public static String generateDreamId() {
        return generateId("DRM");
    }

    /**
     * Generate Restriction ID
     * Format: RST-{timestamp}-{random}
     * Example: RST-1234567890-E1Q6R
     */
    public static String generateRestrictionId() {
        return generateId("RST");
    }

    /**
     * Generate Breakup ID
     * Format: BRK-{timestamp}-{random}
     * Example: BRK-1234567890-F8T2S
     */
    public static String generateBreakupId() {
        return generateId("BRK");
    }

    /**
     * Core method to generate ID with prefix
     */
    private static String generateId(String prefix) {
        long timestamp = Instant.now().toEpochMilli();
        String randomPart = generateRandomString(5);
        return String.format("%s-%d-%s", prefix, timestamp, randomPart);
    }

    /**
     * Generate random alphanumeric string
     */
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Generate UUID-based ID (alternative approach)
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}