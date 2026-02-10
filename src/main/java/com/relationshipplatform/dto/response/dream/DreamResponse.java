package com.relationshipplatform.dto.response.dream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime achievedAt; // When dream was achieved
    
    // Optional: Include basic couple info
    private String user1Name;
    private String user2Name;
    
    // Computed fields
    private boolean isAchieved;
    private Long daysActive; // How long dream has been active
}