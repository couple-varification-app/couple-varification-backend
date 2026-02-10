package com.relationshipplatform.dto.request.breakup;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for initiating a breakup
 * One partner initiates, relationship status changes to BROKEN
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreakupRequestDto {

    @NotBlank(message = "Couple ID is required")
    private String coupleId;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    private String reason; // Optional: reason for breakup

    private boolean allowPatchup; // Whether to allow patch-up after cooldown
}