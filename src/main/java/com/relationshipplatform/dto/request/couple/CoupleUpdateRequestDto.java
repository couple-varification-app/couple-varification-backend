package com.relationshipplatform.dto.request.couple;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating couple information
 * Both partners must approve PIN changes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoupleUpdateRequestDto {

    @NotBlank(message = "Couple ID is required")
    private String coupleId;

    @NotBlank(message = "Current Couple PIN is required")
    private String currentPin;

    @Pattern(regexp = "^[0-9]{6}$", message = "New Couple PIN must be exactly 6 digits")
    private String newPin; // Optional: change PIN

    @Pattern(regexp = "^[0-9]{6}$", message = "Confirm PIN must be exactly 6 digits")
    private String confirmNewPin; // Must match newPin

    private String relationshipStartDate; // Optional: update start date
}