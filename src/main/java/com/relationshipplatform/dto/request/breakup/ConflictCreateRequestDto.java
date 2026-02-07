package com.relationshipplatform.dto.request.breakup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for raising a conflict/issue
 * From improvements document: Conflict Resolution Module
 * Instead of breakup-first thinking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCreateRequestDto {

    @NotBlank(message = "Couple ID is required")
    private String coupleId;

    @NotBlank(message = "Conflict title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Conflict description is required")
    @Size(min = 20, max = 1000, message = "Description must be between 20 and 1000 characters")
    private String description;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    private String severity; // Optional: LOW, MEDIUM, HIGH, CRITICAL

    private Integer coolingOffHours; // Optional: 24, 48, or 72 hours

    private Boolean requestCounseling; // Optional: whether to involve counselor
}