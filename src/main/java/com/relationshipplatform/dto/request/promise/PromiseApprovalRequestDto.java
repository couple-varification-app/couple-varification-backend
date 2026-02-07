package com.relationshipplatform.dto.request.promise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for partner to approve or reject a promise
 * Both partners must approve for promise to become ACTIVE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromiseApprovalRequestDto {

    @NotBlank(message = "Promise ID is required")
    private String promiseId;

    @NotBlank(message = "Couple PIN is required for verification")
    private String couplePin;

    @NotNull(message = "Approval decision is required")
    private Boolean approved; // true = approve, false = reject

    private String rejectionReason; // Optional: if rejected, why?
}