package com.relationshipplatform.dto.request.dream;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a shared dream/goal
 * Either partner can create, no approval needed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DreamCreateRequestDto {

    @NotBlank(message = "Couple ID is required")
    private String coupleId;

    @NotBlank(message = "Dream description is required")
    @Size(min = 10, max = 500, message = "Dream description must be between 10 and 500 characters")
    private String description;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    private String category; // Optional: TRAVEL, FINANCIAL, FAMILY, CAREER, etc.

    private String targetDate; // Optional: when to achieve by (ISO date)
}