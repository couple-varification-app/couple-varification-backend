package com.relationshipplatform.utility;

import java.time.LocalDate;
import java.time.Period;

/**
 * Utility class to calculate age from date of birth
 */
public class AgeCalculator {

    private static final int MINIMUM_AGE = 18;

    /**
     * Calculate age from date of birth
     */
    public static int calculateAge(LocalDate dob) {
        if (dob == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        return Period.between(dob, LocalDate.now()).getYears();
    }

    /**
     * Check if user meets minimum age requirement (18+)
     */
    public static boolean isEligibleAge(LocalDate dob) {
        return calculateAge(dob) >= MINIMUM_AGE;
    }

    /**
     * Get minimum age requirement
     */
    public static int getMinimumAge() {
        return MINIMUM_AGE;
    }
}