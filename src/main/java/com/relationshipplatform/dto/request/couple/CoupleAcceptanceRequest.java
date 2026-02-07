package com.relationshipplatform.dto.request.couple;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for partner to accept couple request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoupleAcceptanceRequest {

    @NotBlank(message = "Couple ID is required")
    private String coupleId;

    @NotBlank(message = "Couple PIN is required")
    private String couplePin;

    private boolean accepted; // true = accept, false = reject
}