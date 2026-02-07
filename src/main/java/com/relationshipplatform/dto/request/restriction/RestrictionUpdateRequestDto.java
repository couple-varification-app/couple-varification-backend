package com.relationshipplatform.dto.request.restriction;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating restriction
 * Extend end date, revoke early, or modify terms
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionUpdateRequestDto {

    @NotBlank(message = "Restriction ID is required")
    private String restrictionId;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    private String newStatus; // Optional: REVOKED, EXTENDED

    private LocalDate newEndDate; // Optional: extend or shorten duration

    private String newDescription; // Optional: modify restriction text

    private Boolean mutualConsent; // Required for changes

    private String updateReason; // Optional: reason for update
}