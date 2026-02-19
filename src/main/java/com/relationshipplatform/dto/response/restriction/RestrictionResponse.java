package com.relationshipplatform.dto.response.restriction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Response DTO for Restriction
 * Time-bound restrictions with auto-expiry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionResponse {

    /**
     * Public restriction ID
     */
    private String restrictionId;

    /**
     * Public couple ID
     */
    private String coupleId;

    /**
     * Restriction description/rule
     */
    private String description;

    /**
     * Restriction status
     * Values: PENDING, ACTIVE, EXPIRED, REVOKED
     */
    private String status;

    /**
     * User 1 approval status
     */
    private boolean user1Approved;

    /**
     * User 2 approval status
     */
    private boolean user2Approved;

    /**
     * Start date (when restriction becomes active)
     */
    private LocalDate startDate;

    /**
     * End date (when restriction auto-expires)
     */
    private LocalDate endDate;

    /**
     * Optional: Category
     * Values: SOCIAL, FINANCIAL, TIME, BEHAVIOR, COMMUNICATION, etc.
     */
    private String category;

    /**
     * Optional: Severity level
     * Values: LIGHT, MODERATE, STRICT
     */
    private String severity;

    /**
     * Auto-renewal flag
     * If true, restriction renews after expiry (requires re-approval)
     */
    private boolean autoRenew;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    // ========================================
    // COMPUTED FIELDS
    // ========================================

    /**
     * Check if restriction is fully approved
     */
    public boolean isFullyApproved() {
        return user1Approved && user2Approved;
    }

    /**
     * Check if restriction is pending
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * Check if restriction is active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Check if restriction is expired
     */
    public boolean isExpired() {
        return "EXPIRED".equals(status);
    }

    /**
     * Check if restriction is revoked
     */
    public boolean isRevoked() {
        return "REVOKED".equals(status);
    }

    /**
     * Get days remaining until expiry
     */
    public Long getDaysRemaining() {
        if (endDate == null || !isActive()) {
            return null;
        }
        
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return days >= 0 ? days : 0;
    }

    /**
     * Get total duration in days
     */
    public Long getTotalDurationDays() {
        if (startDate == null || endDate == null) {
            return null;
        }
        
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Check if restriction is expiring soon (within 7 days)
     */
    public boolean isExpiringSoon() {
        Long daysRemaining = getDaysRemaining();
        return daysRemaining != null && daysRemaining <= 7 && daysRemaining > 0;
    }

    /**
     * Check if restriction has started
     */
    public boolean hasStarted() {
        if (startDate == null) {
            return false;
        }
        return !LocalDate.now().isBefore(startDate);
    }

    /**
     * Check if restriction is currently in effect
     */
    public boolean isCurrentlyInEffect() {
        if (!isActive() || startDate == null || endDate == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Get status display text
     */
    public String getStatusDisplayText() {
        switch (status) {
            case "PENDING":
                return "Pending Approval";
            case "ACTIVE":
                if (isExpiringSoon()) {
                    return "Active (Expiring Soon)";
                }
                return "Active";
            case "EXPIRED":
                return "Expired ⏰";
            case "REVOKED":
                return "Revoked";
            default:
                return status;
        }
    }

    /**
     * Get time remaining text
     */
    public String getTimeRemainingText() {
        Long days = getDaysRemaining();
        if (days == null) {
            return null;
        }
        
        if (days == 0) {
            return "Expires today";
        } else if (days == 1) {
            return "1 day remaining";
        } else if (days < 7) {
            return String.format("%d days remaining", days);
        } else if (days < 30) {
            long weeks = days / 7;
            return String.format("%d week%s remaining", weeks, weeks == 1 ? "" : "s");
        } else {
            long months = days / 30;
            return String.format("%d month%s remaining", months, months == 1 ? "" : "s");
        }
    }

    /**
     * Get severity badge color
     */
    public String getSeverityColor() {
        if (severity == null) {
            return "gray";
        }
        
        switch (severity.toUpperCase()) {
            case "LIGHT":
                return "green";
            case "MODERATE":
                return "yellow";
            case "STRICT":
                return "red";
            default:
                return "gray";
        }
    }
}