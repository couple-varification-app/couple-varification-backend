package com.relationshipplatform.dto.response.couple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.relationshipplatform.dto.response.user.UserResponse;

/**
 * DTO for couple data in responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleResponse {

    private String coupleId;
    private UserResponse user1;
    private UserResponse user2;
    private String status; // PENDING, ACTIVE, BROKEN
    private Integer relationshipHealth;
    private Integer loyaltyScore;
    private LocalDateTime createdAt;
    private boolean isPending; // true if waiting for partner acceptance
}