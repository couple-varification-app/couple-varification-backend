package com.relationshipplatform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.relationshipplatform.dto.request.couple.CoupleCreateRequestDto;
import com.relationshipplatform.dto.response.api.ApiResponse;
import com.relationshipplatform.dto.response.couple.CoupleResponse;
import com.relationshipplatform.service.CoupleService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/couples")
public class CoupleController {

    private final CoupleService coupleService;

    public CoupleController(CoupleService coupleService) {
        this.coupleService = coupleService;
    }



    @PostMapping("/request")
    public ResponseEntity<ApiResponse<CoupleResponse>> createCoupleRequest(
            @RequestHeader("X-User-Id") String initiatorUserId,
            @Valid @RequestBody CoupleCreateRequestDto request) {
        
        log.info("POST /api/couples/request - Initiator: {}, Partner: {}", initiatorUserId, request.getPartnerUserId());
        
        CoupleResponse couple = coupleService.createCoupleRequest(initiatorUserId, request);
        ApiResponse<CoupleResponse> response = ApiResponse.success(couple, "Couple request created. Awaiting partner acceptance.");
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
}
