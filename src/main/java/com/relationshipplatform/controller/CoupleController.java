package com.relationshipplatform.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.relationshipplatform.dto.request.couple.CoupleAcceptRequestDto;
import com.relationshipplatform.dto.request.couple.CoupleCreateRequestDto;
import com.relationshipplatform.dto.request.couple.CoupleUpdateRequestDto;
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



    /**
     * Create couple request (initiator perspective)
     * POST /api/couples/request
     * Header: X-User-Id (initiator's user ID)
     * 
     * Request Body:
     * {
     *   "partnerUserId": "USR-9876543210-XYZ",
     *   "couplePin": "123456",
     *   "confirmPin": "123456"
     * }
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<CoupleResponse>> createCoupleRequest(
            @RequestHeader("X-User-Id") String initiatorUserId,
            @Valid @RequestBody CoupleCreateRequestDto request) {
        
        log.info("POST /api/couples/request - Initiator: {}, Partner: {}", initiatorUserId, request.getPartnerUserId());
        
        CoupleResponse couple = coupleService.createCoupleRequest(initiatorUserId, request);
        ApiResponse<CoupleResponse> response = ApiResponse.success(couple, "Couple request created. Awaiting partner acceptance.");
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    

    /**
     * Accept or reject couple request (partner perspective)
     * POST /api/couples/respond
     * Header: X-User-Id (partner's user ID)
     * 
     * Request Body:
     * {
     *   "coupleId": "CPL-1234567890-ABC",
     *   "couplePin": "123456",
     *   "accepted": true
     * }
     */
    @PostMapping("/respond")
    public ResponseEntity<ApiResponse<CoupleResponse>> respondToCoupleRequest(
            @RequestHeader("X-User-Id") String partnerUserId,
            @Valid @RequestBody CoupleAcceptRequestDto request) {
        
        log.info("POST /api/couples/respond - Partner: {}, Couple: {}, Accepted: {}", 
                 partnerUserId, request.getCoupleId(), request.isAccepted());
        
        CoupleResponse couple = coupleService.respondToCoupleRequest(partnerUserId, request);
        
        if (couple == null) {
            // Couple was rejected
            ApiResponse<CoupleResponse> response = ApiResponse.success(
                null, 
                "Couple request rejected"
            );
            return ResponseEntity.ok(response);
        }
        
        // Couple was accepted
        ApiResponse<CoupleResponse> response = ApiResponse.success(
            couple, 
            "Couple activated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get couple by ID
     * GET /api/couples/{coupleId}
     */
    @GetMapping("/{coupleId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<CoupleResponse>> getCoupleById(
            @PathVariable String coupleId) {
        
        log.info("GET /api/couples/{} - Fetching couple", coupleId);
        
        CoupleResponse couple = coupleService.getCoupleById(coupleId);
        ApiResponse<CoupleResponse> response = ApiResponse.success(
            couple, 
            "Couple retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

     /**
     * Get active couple for a user
     * GET /api/couples/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<CoupleResponse>> getActiveCoupleByUser(
            @PathVariable String userId) {
        
        log.info("GET /api/couples/user/{} - Fetching active couple", userId);
        
        CoupleResponse couple = coupleService.getActiveCoupleByUser(userId);
        
        if (couple == null) {
            ApiResponse<CoupleResponse> response = ApiResponse.success(
                null, 
                "User is not in any active couple"
            );
            return ResponseEntity.ok(response);
        }
        
        ApiResponse<CoupleResponse> response = ApiResponse.success(
            couple, 
            "Active couple retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending couple request for a user
     * GET /api/couples/user/{userId}/pending
     */
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<ApiResponse<CoupleResponse>> getPendingCoupleRequest(
            @PathVariable String userId) {
        
        log.info("GET /api/couples/user/{}/pending - Fetching pending request", userId);
        
        CoupleResponse couple = coupleService.getPendingCoupleRequestForUser(userId);
        
        if (couple == null) {
            ApiResponse<CoupleResponse> response = ApiResponse.success(
                null, 
                "No pending couple request"
            );
            return ResponseEntity.ok(response);
        }
        
        ApiResponse<CoupleResponse> response = ApiResponse.success(
            couple, 
            "Pending couple request retrieved"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update couple information (PIN change, etc.)
     * PUT /api/couples
     * 
     * Request Body:
     * {
     *   "coupleId": "CPL-1234567890-ABC",
     *   "currentPin": "123456",
     *   "newPin": "654321",
     *   "confirmNewPin": "654321"
     * }
     */
    @PutMapping
    public ResponseEntity<ApiResponse<CoupleResponse>> updateCouple(
            @Valid @RequestBody CoupleUpdateRequestDto request) {
        
        log.info("PUT /api/couples - Updating couple: {}", request.getCoupleId());
        
        CoupleResponse couple = coupleService.updateCouple(request);
        ApiResponse<CoupleResponse> response = ApiResponse.success(
            couple, 
            "Couple updated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verify couple PIN (for secure operations)
     * POST /api/couples/{coupleId}/verify-pin
     * 
     * Request Param: pin=123456
     */
    @PostMapping("/{coupleId}/verify-pin")
    public ResponseEntity<ApiResponse<Boolean>> verifyCouplePin(
            @PathVariable String coupleId,
            @RequestParam String pin) {
        
        log.info("POST /api/couples/{}/verify-pin - Verifying PIN", coupleId);
        
        boolean isValid = coupleService.verifyCouplePin(coupleId, pin);
        ApiResponse<Boolean> response = ApiResponse.success(
            isValid, 
            isValid ? "PIN verified successfully" : "Invalid PIN"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update relationship health (admin/system operation)
     * PUT /api/couples/{coupleId}/health
     * 
     * Request Param: score=85
     */
    @PutMapping("/{coupleId}/health")
    public ResponseEntity<ApiResponse<Void>> updateRelationshipHealth(
            @PathVariable String coupleId,
            @RequestParam int score) {
        
        log.info("PUT /api/couples/{}/health - New score: {}", coupleId, score);
        
        coupleService.updateRelationshipHealth(coupleId, score);
        ApiResponse<Void> response = ApiResponse.success(
            "Relationship health updated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update loyalty score (admin/system operation)
     * PUT /api/couples/{coupleId}/loyalty
     * 
     * Request Param: score=90
     */
    @PutMapping("/{coupleId}/loyalty")
    public ResponseEntity<ApiResponse<Void>> updateLoyaltyScore(
            @PathVariable String coupleId,
            @RequestParam int score) {
        
        log.info("PUT /api/couples/{}/loyalty - New score: {}", coupleId, score);
        
        coupleService.updateLoyaltyScore(coupleId, score);
        ApiResponse<Void> response = ApiResponse.success(
            "Loyalty score updated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all couples (admin only)
     * GET /api/couples
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")  // 🔒 Admin only
    public ResponseEntity<ApiResponse<List<CoupleResponse>>> getAllCouples() {
        
        log.info("GET /api/couples - Fetching all couples");
        
        List<CoupleResponse> couples = coupleService.getAllCouples();
        ApiResponse<List<CoupleResponse>> response = ApiResponse.success(
            couples, 
            String.format("%d couples retrieved", couples.size())
        );
        
        return ResponseEntity.ok(response);
    }

}
