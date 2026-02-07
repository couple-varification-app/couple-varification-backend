package com.relationshipplatform.dto.request.couple;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for couple registration request
 * One user initiates, partner must accept
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoupleRegistrationRequest {

    @NotBlank(message = "Partner User ID is required")
    private String partnerUserId;

    @NotBlank(message = "Couple PIN is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Couple PIN must be exactly 6 digits")
    private String couplePin;

    @NotBlank(message = "Confirm PIN is required")
    private String confirmPin;
    
    private String relationshipStartDate; // Optional: when relationship actually started
}