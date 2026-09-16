package com.relationshipplatform.mapper;

import org.springframework.stereotype.Component;

import com.relationshipplatform.dto.response.dream.DreamResponse;
import com.relationshipplatform.entity.Dream;

@Component
public class DreamMapper {
    

    // Convert Dream entity to DreamResponse DTO

    public DreamResponse toResponse(Dream dream){
        if(dream == null){
            return null;
        }

        return DreamResponse.builder()
                .dreamId(dream.getDreamId())
                .coupleId(dream.getCouple().getCoupleId())
                .description(dream.getDescription())
                .status(dream.getStatus())
                .category(dream.getCategory())
                .createdAt(dream.getCreatedAt())
                .targetDate(dream.getTargetDate())
                .updatedAt(dream.getUpdatedAt())
                .achievedAt(dream.getAchievedAt())
                .build();

    }
}
