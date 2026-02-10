package com.relationshipplatform.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.relationshipplatform.dto.request.promise.PromiseApprovalRequestDto;
import com.relationshipplatform.dto.request.promise.PromiseCreateRequestDto;
import com.relationshipplatform.dto.request.promise.PromiseUpdateRequestDto;
import com.relationshipplatform.dto.response.api.ApiResponse;
import com.relationshipplatform.dto.response.promise.PromiseResponse;
import com.relationshipplatform.service.PromiseService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/promises")
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
public class PromiseController {

    private final PromiseService promiseService;

    public PromiseController(PromiseService promiseService){
        this.promiseService = promiseService;
    }

    /**
     * Create a new promise
     * POST /api/promises
     * 
     * Header: X-User-Id (initiator's user ID)
     * 
     * Request Body:
     * {
     *   "coupleId": "CPL-xxx",
     *   "description": "We promise to communicate openly",
     *   "couplePin": "123456",
     *   "priority": "HIGH",
     *   "category": "COMMUNICATION"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PromiseResponse>> createPromise(
        @RequestHeader("X-User-Id") String initiatorUserId,
        @Valid @RequestBody PromiseCreateRequestDto request){

            log.info("POST /api/promises - Creating promise for couple: ",request.getCoupleId());

            PromiseResponse promise = promiseService.createPromise(initiatorUserId, request);
            ApiResponse<PromiseResponse> response = ApiResponse.success(promise, "Promise created. Awaiting partner approval.");

            return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    
    /**
     * Approve or reject a promise
     * POST /api/promises/approve
     * 
     * Header: X-User-Id (partner's user ID)
     * 
     * Request Body:
     * {
     *   "promiseId": "PRM-xxx",
     *   "couplePin": "123456",
     *   "approved": true
     * }
     */
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<PromiseResponse>> approvePromise(
            @RequestHeader("X-User-Id") String partnerUserId,
            @Valid @RequestBody PromiseApprovalRequestDto request) {
        
        log.info("POST /api/promises/approve - Partner: {}, Promise: {}, Approved: {}",
                 partnerUserId, request.getPromiseId(), request.getApproved());
        
        PromiseResponse promise = promiseService.approvePromise(partnerUserId, request);
        
        if (promise == null) {
            // Promise was rejected
            ApiResponse<PromiseResponse> response = ApiResponse.success(
                null,
                "Promise rejected"
            );
            return ResponseEntity.ok(response);
        }
        
        // Promise was approved
        ApiResponse<PromiseResponse> response = ApiResponse.success(
            promise,
            promise.isActive() ? "Promise activated successfully" : "Promise approved, awaiting other partner"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get promise by ID
     * GET /api/promises/{promiseId}
     */
    @GetMapping("/{promiseId}")
    public ResponseEntity<ApiResponse<PromiseResponse>> getPromiseById(
            @PathVariable String promiseId) {
        
        log.info("GET /api/promises/{} - Fetching promise", promiseId);
        
        PromiseResponse promise = promiseService.getPromiseById(promiseId);
        ApiResponse<PromiseResponse> response = ApiResponse.success(
            promise,
            "Promise retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all promises for a couple
     * GET /api/promises/couple/{coupleId}
     */
    @GetMapping("/couple/{coupleId}")
    public ResponseEntity<ApiResponse<List<PromiseResponse>>> getPromisesByCouple(
            @PathVariable String coupleId) {
        
        log.info("GET /api/promises/couple/{} - Fetching all promises", coupleId);
        
        List<PromiseResponse> promises = promiseService.getPromisesByCouple(coupleId);
        ApiResponse<List<PromiseResponse>> response = ApiResponse.success(
            promises,
            String.format("%d promise(s) retrieved", promises.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get active promises for a couple
     * GET /api/promises/couple/{coupleId}/active
     */
    @GetMapping("/couple/{coupleId}/active")
    public ResponseEntity<ApiResponse<List<PromiseResponse>>> getActivePromises(
            @PathVariable String coupleId) {
        
        log.info("GET /api/promises/couple/{}/active - Fetching active promises", coupleId);
        
        List<PromiseResponse> promises = promiseService.getActivePromisesByCouple(coupleId);
        ApiResponse<List<PromiseResponse>> response = ApiResponse.success(
            promises,
            String.format("%d active promise(s)", promises.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending promises for a couple
     * GET /api/promises/couple/{coupleId}/pending
     */
    @GetMapping("/couple/{coupleId}/pending")
    public ResponseEntity<ApiResponse<List<PromiseResponse>>> getPendingPromises(
            @PathVariable String coupleId) {
        
        log.info("GET /api/promises/couple/{}/pending - Fetching pending promises", coupleId);
        
        List<PromiseResponse> promises = promiseService.getPendingPromisesByCouple(coupleId);
        ApiResponse<List<PromiseResponse>> response = ApiResponse.success(
            promises,
            String.format("%d pending promise(s)", promises.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get promises by status
     * GET /api/promises/couple/{coupleId}/status/{status}
     * 
     * Status values: PENDING, ACTIVE, COMPLETED, BROKEN
     */
    @GetMapping("/couple/{coupleId}/status/{status}")
    public ResponseEntity<ApiResponse<List<PromiseResponse>>> getPromisesByStatus(
            @PathVariable String coupleId,
            @PathVariable String status) {
        
        log.info("GET /api/promises/couple/{}/status/{}", coupleId, status);
        
        List<PromiseResponse> promises = promiseService.getPromisesByStatus(coupleId, status);
        ApiResponse<List<PromiseResponse>> response = ApiResponse.success(
            promises,
            String.format("%d %s promise(s)", promises.size(), status.toLowerCase())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update promise status (mark as COMPLETED or BROKEN)
     * PUT /api/promises/{promiseId}/status
     * 
     * Request Body:
     * {
     *   "promiseId": "PRM-xxx",
     *   "couplePin": "123456",
     *   "newStatus": "COMPLETED",
     *   "updateReason": "We did it!"
     * }
     */
    @PutMapping("/{promiseId}/status")
    public ResponseEntity<ApiResponse<PromiseResponse>> updatePromiseStatus(
            @PathVariable String promiseId,
            @Valid @RequestBody PromiseUpdateRequestDto request) {
        
        log.info("PUT /api/promises/{}/status - New status: {}", promiseId, request.getNewStatus());
        
        // Ensure promiseId matches
        request.setPromiseId(promiseId);
        
        PromiseResponse promise = promiseService.updatePromiseStatus(request);
        ApiResponse<PromiseResponse> response = ApiResponse.success(
            promise,
            "Promise status updated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a promise
     * DELETE /api/promises/{promiseId}
     * 
     * Query Param: pin=123456
     */
    @DeleteMapping("/{promiseId}")
    public ResponseEntity<ApiResponse<Void>> deletePromise(
            @PathVariable String promiseId,
            @RequestParam String pin) {
        
        log.info("DELETE /api/promises/{} - Deleting promise", promiseId);
        
        promiseService.deletePromise(promiseId, pin);
        ApiResponse<Void> response = ApiResponse.success("Promise deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get promise statistics for a couple
     * GET /api/promises/couple/{coupleId}/stats
     * 
     * Returns counts of completed and broken promises
     */
    @GetMapping("/couple/{coupleId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPromiseStats(
            @PathVariable String coupleId) {
        
        log.info("GET /api/promises/couple/{}/stats", coupleId);
        
        long completedCount = promiseService.getCompletedPromisesCount(coupleId);
        long brokenCount = promiseService.getBrokenPromisesCount(coupleId);
        long totalCount = completedCount + brokenCount;
        double completionRate = totalCount > 0 ? (completedCount * 100.0 / totalCount) : 0.0;
        
        Map<String, Object> stats = Map.of(
            "completedCount", completedCount,
            "brokenCount", brokenCount,
            "totalCount", totalCount,
            "completionRate", Math.round(completionRate * 10) / 10.0
        );
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(
            stats,
            "Promise statistics retrieved"
        );
        
        return ResponseEntity.ok(response);
    }
    
}
