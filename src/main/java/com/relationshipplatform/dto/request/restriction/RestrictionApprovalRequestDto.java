package com.relationshipplatform.dto.request.restriction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for partner to approve or reject a restriction
 * Both partners must approve for restriction to become ACTIVE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionApprovalRequestDto {

    @NotBlank(message = "Restriction ID is required")
    private String restrictionId;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    @NotNull(message = "Approval decision is required")
    private Boolean approved; // true = approve, false = reject

    private String rejectionReason; // Optional: if rejected, why?

    private String counterProposal; // Optional: suggest modifications
}