package com.relationshipplatform.mapper;

import com.relationshipplatform.dto.response.restriction.RestrictionResponse;
import com.relationshipplatform.entity.Restriction;
import org.springframework.stereotype.Component;

/**
 * Mapper for Restriction entity to RestrictionResponse DTO
 */
@Component
public class RestrictionMapper {

    /**
     * Convert Restriction entity to RestrictionResponse DTO
     */
    public RestrictionResponse toResponse(Restriction restriction) {
        if (restriction == null) {
            return null;
        }

        return RestrictionResponse.builder()
                .restrictionId(restriction.getRestrictionId())
                .coupleId(restriction.getCouple() != null ? restriction.getCouple().getCoupleId() : null)
                .description(restriction.getDescription())
                .status(restriction.getStatus())
                .user1Approved(restriction.isUser1Approved())
                .user2Approved(restriction.isUser2Approved())
                .startDate(restriction.getStartDate())
                .endDate(restriction.getEndDate())
                .category(restriction.getCategory())
                // do this later
                // .severity(restriction.getSeverity())
                // .autoRenew(restriction.isAutoRenew())
                // .createdAt(restriction.getCreatedAt())
                // .updatedAt(restriction.getUpdatedAt())
                .build();
    }
}