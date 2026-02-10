package com.relationshipplatform.dto.request.promise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a promise
 * Requires mutual approval from both partners
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromiseCreateRequestDto {

    @NotBlank(message = "Couple ID is required")
    private String coupleId;

    @NotBlank(message = "Promise description is required")
    @Size(min = 10, max = 500, message = "Promise description must be between 10 and 500 characters")
    private String description;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    private String priority; // Optional: HIGH, MEDIUM, LOW

    private String category; // Optional: COMMUNICATION, TRUST, TIME, FINANCIAL, etc.

    private String expiryDate; // Optional: deadline for promise (ISO date)
}