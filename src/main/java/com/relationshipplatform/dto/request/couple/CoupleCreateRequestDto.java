package com.relationshipplatform.dto.request.couple;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for couple registration request
 * One user initiates, partner must accept
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleCreateRequestDto {

    @NotBlank(message = "Partner User ID is required")
    @Pattern(
        regexp = "^USR-\\d+-[A-Z0-9]+$",
        message = "Invalid User ID format. Expected format: USR-{timestamp}-{random}"
    )
    private String partnerUserId;

    @NotBlank(message = "Couple PIN is required")
    @Pattern(
        regexp = "^[0-9]{6}$",
        message = "Couple PIN must be exactly 6 digits"
    )
    @Size(min = 6, max = 6, message = "Couple PIN must be exactly 6 digits")
    private String couplePin;

    @NotBlank(message = "Confirm PIN is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Confirm PIN must be exactly 6 digits")
    @Size(min = 6, max = 6, message = "Confirm PIN must be exactly 6 digits")
    private String confirmPin;

    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "Relationship start date must be in format YYYY-MM-DD"
    )
    private String relationshipStartDate;

    @Size(max = 200, message = "Message must not exceed 200 characters")
    private String messageToPartner;

    @Size(max = 50, message = "Relationship type must not exceed 50 characters")
    private String relationshipType;

    @Override
    public String toString() {
        return "CoupleCreateRequestDto{" +
                "partnerUserId='" + partnerUserId + '\'' +
                ", couplePin='******'" +
                ", confirmPin='******'" +
                ", relationshipStartDate='" + relationshipStartDate + '\'' +
                ", messageToPartner='" + messageToPartner + '\'' +
                ", relationshipType='" + relationshipType + '\'' +
                '}';
    }
}