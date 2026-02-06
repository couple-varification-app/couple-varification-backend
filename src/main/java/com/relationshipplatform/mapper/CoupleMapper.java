package com.relationshipplatform.mapper;

import com.relationshipplatform.entity.Couple;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper to convert Couple entity to CoupleResponse DTO
 */
@Component
public class CoupleMapper {

    private final UserMapper userMapper;

    public CoupleMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * Convert Couple entity to CoupleResponse DTO
     */
    public com.relationshipplatform.dto.response.couple.CoupleResponse toResponse(Couple couple) {
        if (couple == null) {
            return null;
        }

        boolean isPending = "PENDING".equals(couple.getStatus());

        return com.relationshipplatform.dto.response.couple.CoupleResponse.builder()
                .coupleId(couple.getCoupleId())
                .user1(userMapper.toResponse(couple.getUser1()))
                .user2(userMapper.toResponse(couple.getUser2()))
                .status(couple.getStatus())
                .relationshipHealth(couple.getRelationshipHealth())
                .loyaltyScore(couple.getLoyaltyScore())
                .createdAt(LocalDateTime.now()) // You may need to add createdAt field to Couple entity
                .isPending(isPending)
                .build();
    }
}