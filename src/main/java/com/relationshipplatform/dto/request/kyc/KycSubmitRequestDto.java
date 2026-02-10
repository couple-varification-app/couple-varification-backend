package com.relationshipplatform.dto.request.kyc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submitting KYC (Know Your Couple) verification
 * Both partners must confirm annually
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycSubmitRequestDto {

    @NotBlank(message = "Couple ID is required")
    private String coupleId;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    @NotNull(message = "Year is required")
    private Integer year; // Anniversary year (1, 2, 3, ...)

    @NotNull(message = "Confirmation is required")
    private Boolean confirmed; // User confirms they're still in relationship

    private String relationshipStatus; // Optional: EXCELLENT, GOOD, FAIR, etc.

    private String notes; // Optional: any notes for this year
}