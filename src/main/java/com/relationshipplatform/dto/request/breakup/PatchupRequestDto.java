package com.relationshipplatform.dto.request.breakup;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for patch-up request after breakup
 * Both partners must agree to patch-up
 * Only allowed after 6-month cooldown period
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatchupRequestDto {

    @NotBlank(message = "Breakup ID is required")
    private String breakupId;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    private String message; // Optional: message to partner

    private Boolean mutualConsent; // Both must agree
}