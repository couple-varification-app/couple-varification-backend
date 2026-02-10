package com.relationshipplatform.dto.request.promise;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating promise status
 * Mark as completed, broken, or modify description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromiseUpdateRequestDto {

    @NotBlank(message = "Promise ID is required")
    private String promiseId;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    private String newStatus; // Optional: COMPLETED, BROKEN

    private String newDescription; // Optional: modify promise text

    private Boolean mutualConsent; // Required for status changes

    private String updateReason; // Optional: reason for update
}