package com.relationshipplatform.dto.response.breakup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Breakup entity
 * Includes 6-month cooldown and patch-up information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakupResponse {

    private String breakupId;
    private String coupleId;
    private String initiatedByUserId;
    private String initiatedByName;
    private LocalDate breakupDate;
    private boolean patchupAllowed;
    private LocalDate cooldownEndDate; // 6 months from breakup
    private String status; // ACTIVE, PATCHED_UP
    private LocalDateTime createdAt;
    
    // Partner info
    private String otherPartnerUserId;
    private String otherPartnerName;
    
    // Computed fields
    private boolean inCooldownPeriod; // true if within 6 months
    private Long daysUntilCooldownEnd;
    private boolean canPatchup; // true if patchupAllowed and cooldown ended
}