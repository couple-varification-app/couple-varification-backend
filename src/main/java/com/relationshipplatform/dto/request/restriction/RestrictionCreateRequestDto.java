package com.relationshipplatform.dto.request.restriction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a restriction
 * Time-bound restriction that requires mutual approval
 * Based on improvements document: restrictions should have start/end dates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionCreateRequestDto {

    @NotBlank(message = "Couple ID is required")
    private String coupleId;

    @NotBlank(message = "Restriction description is required")
    @Size(min = 10, max = 500, message = "Restriction description must be between 10 and 500 characters")
    private String description;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String category; // Optional: SOCIAL_MEDIA, COMMUNICATION, BEHAVIOR, TIME, etc.

    private String severity; // Optional: STRICT, MODERATE, FLEXIBLE

    private Boolean autoRenew; // Optional: automatically renew when expires
}