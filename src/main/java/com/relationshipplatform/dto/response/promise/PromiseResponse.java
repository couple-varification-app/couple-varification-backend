package com.relationshipplatform.dto.response.promise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Promise entity
 * Includes approval status and consent information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromiseResponse {

    private String promiseId;
    private String coupleId;
    private String description;
    private boolean user1Approved;
    private boolean user2Approved;
    private String status; // ACTIVE, BROKEN, COMPLETED, PENDING
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private boolean fullyApproved; // true if both users approved
    private boolean isPending; // true if waiting for approval
    
    // Optional: Include basic couple info
    private String user1Name;
    private String user2Name;
}