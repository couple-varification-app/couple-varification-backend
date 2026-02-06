package com.relationshipplatform.mapper;

import com.relationshipplatform.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert User entity to UserResponse DTO
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserResponse DTO
     * Excludes sensitive information like password
     */
    public com.relationshipplatform.dto.response.user.UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        String currentCoupleId = null;
        
        // Check if user is part of any active couple
        if (user.getCoupleAsUser1() != null && "ACTIVE".equals(user.getCoupleAsUser1().getStatus())) {
            currentCoupleId = user.getCoupleAsUser1().getCoupleId();
        } else if (user.getCoupleAsUser2() != null && "ACTIVE".equals(user.getCoupleAsUser2().getStatus())) {
            currentCoupleId = user.getCoupleAsUser2().getCoupleId();
        }

        return com.relationshipplatform.dto.response.user.UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .age(user.getAge())
                .verified(user.isVerified())
                .active(user.isActive())
                .currentCoupleId(currentCoupleId)
                .build();
    }
}