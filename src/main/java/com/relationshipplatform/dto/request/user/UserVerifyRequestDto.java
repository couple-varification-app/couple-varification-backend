package com.relationshipplatform.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserVerifyRequestDto {

    @NotNull(message = "Verified flag is required")
    private Boolean verified;
}