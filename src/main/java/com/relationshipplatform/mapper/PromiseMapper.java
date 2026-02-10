package com.relationshipplatform.mapper;

import org.springframework.stereotype.Component;

import com.relationshipplatform.dto.response.promise.PromiseResponse;
import com.relationshipplatform.entity.Promise;

@Component
public class PromiseMapper {

    public PromiseResponse toResponse(Promise promise){
        if (promise == null){
            return null;
        }

        return PromiseResponse.builder()
                .promiseId(promise.getPromiseId())
                .coupleId(promise.getCouple() != null ? promise.getCouple().getCoupleId() : null)
                .description(promise.getDescription())
                .status(promise.getStatus())
                .user1Approved(promise.isUser1Approved())
                .user2Approved(promise.isUser2Approved())
                .expiryDate(promise.getExpiryDate() != null ? promise.getExpiryDate().toString() : null)
                .createdAt(promise.getCreatedAt())
                .updatedAt(promise.getUpdatedAt())
                .build();

    }
}
