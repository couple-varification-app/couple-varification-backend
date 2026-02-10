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
    private String expiryDate;
    
    // Computed fields
    // private boolean isFullyApproved; // true if both users approved
    private boolean isPending; // true if waiting for approval
    
    // Optional: Include basic couple info
    private String user1Name;
    private String user2Name;

    public boolean isFullyApproved() {
        return user1Approved && user2Approved;
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isBroken() {
        return "BROKEN".equals(status);
    }

    public String getApprovalStatusText() {
        if (isFullyApproved()) {
            return "Approved by both partners";
        } else if (user1Approved && !user2Approved) {
            return "Awaiting partner approval";
        } else if (!user1Approved && user2Approved) {
            return "Awaiting your approval";
        } else {
            return "Not yet approved";
        }
    }

    public String getStatusDisplayText() {
        switch (status) {
            case "PENDING":
                return "Pending Approval";
            case "ACTIVE":
                return "Active";
            case "COMPLETED":
                return "Completed ✓";
            case "BROKEN":
                return "Broken ✗";
            default:
                return status;
        }

    }

}