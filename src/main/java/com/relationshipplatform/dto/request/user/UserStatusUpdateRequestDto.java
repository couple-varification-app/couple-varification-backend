package com.relationshipplatform.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequestDto {

    @NotNull(message = "Active status is required")
    private Boolean active;
}
