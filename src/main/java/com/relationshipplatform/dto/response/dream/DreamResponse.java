package com.relationshipplatform.dto.response.dream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Response DTO for Dream entity
 * Represents shared goals and aspirations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DreamResponse {

    private String dreamId;
    private String coupleId;
    private String description;
    private String status; // ACTIVE, ACHIEVED, ABANDONED
    private String category; //Values: TRAVEL, FINANCIAL, FAMILY, CAREER, HEALTH, HOME, etc.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate targetDate;
    private LocalDateTime achievedAt; // When dream was achieved
    
    // Optional: Include basic couple info
    private String user1Name;
    private String user2Name;
    
    // Computed fields
    private boolean isAchieved;
    private Long daysActive; // How long dream has been active

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Check if dream is achieved
     */
    public boolean isAchieved() {
        return "ACHIEVED".equals(status);
    }

    /**
     * Check if dream is abandoned
     */
    public boolean isAbandoned() {
        return "ABANDONED".equals(status);
    }

    /**
     * Get days since creation
     */
    public long getDaysActive() {
        if (createdAt == null) return 0;
        
        LocalDateTime endDate = achievedAt != null ? achievedAt : LocalDateTime.now();
        return ChronoUnit.DAYS.between(createdAt, endDate);
    }

    /**
     * Get days until target date
     */
    public Long getDaysUntilTarget() {
        if (targetDate == null || isAchieved() || isAbandoned()) {
            return null;
        }
        
        long days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
        return days >= 0 ? days : null;
    }

    /**
     * Check if target date is overdue
     */
    public boolean isOverdue() {
        if (targetDate == null || isAchieved() || isAbandoned()) {
            return false;
        }
        return LocalDate.now().isAfter(targetDate);
    }

    /**
     * Get status display text
     */
    public String getStatusDisplayText() {
        switch (status) {
            case "ACTIVE":
                return isOverdue() ? "Active (Overdue)" : "Active";
            case "ACHIEVED":
                return "Achieved ✓";
            case "ABANDONED":
                return "Abandoned";
            default:
                return status;
        }
    }

    /**
     * Get achievement time text
     */
    public String getAchievementTimeText() {
        if (!isAchieved() || achievedAt == null) {
            return null;
        }
        
        long days = getDaysActive();
        if (days == 0) {
            return "Achieved today!";
        } else if (days == 1) {
            return "Achieved in 1 day";
        } else if (days < 30) {
            return String.format("Achieved in %d days", days);
        } else if (days < 365) {
            long months = days / 30;
            return String.format("Achieved in %d month%s", months, months == 1 ? "" : "s");
        } else {
            long years = days / 365;
            return String.format("Achieved in %d year%s", years, years == 1 ? "" : "s");
        }
    }
}