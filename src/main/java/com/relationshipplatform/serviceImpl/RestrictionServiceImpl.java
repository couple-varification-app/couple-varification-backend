package com.relationshipplatform.serviceImpl;

import com.relationshipplatform.dto.request.restriction.RestrictionApprovalRequestDto;
import com.relationshipplatform.dto.request.restriction.RestrictionCreateRequestDto;
import com.relationshipplatform.dto.request.restriction.RestrictionUpdateRequestDto;
import com.relationshipplatform.dto.response.restriction.RestrictionResponse;
import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.Restriction;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.exception.*;
import com.relationshipplatform.mapper.RestrictionMapper;
import com.relationshipplatform.repository.CoupleRepository;
import com.relationshipplatform.repository.RestrictionRepository;
import com.relationshipplatform.repository.UserRepository;
import com.relationshipplatform.service.CoupleService;
import com.relationshipplatform.service.RestrictionService;
import com.relationshipplatform.utility.IdGenerator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of RestrictionService
 * Time-bound restrictions with mutual approval and auto-expiry
 */

// ================================= implement the restriction.setUpdatedAt() ====================
@Slf4j
@Service
public class RestrictionServiceImpl implements RestrictionService {

    private final RestrictionRepository restrictionRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final CoupleService coupleService;
    private final RestrictionMapper restrictionMapper;

    public RestrictionServiceImpl(RestrictionRepository restrictionRepository,
                                  CoupleRepository coupleRepository,
                                  UserRepository userRepository,
                                  CoupleService coupleService,
                                  RestrictionMapper restrictionMapper) {
        this.restrictionRepository = restrictionRepository;
        this.coupleRepository = coupleRepository;
        this.userRepository = userRepository;
        this.coupleService = coupleService;
        this.restrictionMapper = restrictionMapper;
    }

    @Override
    @Transactional
    public RestrictionResponse createRestriction(String initiatorUserId, RestrictionCreateRequestDto request) {
        log.info("Creating restriction for couple: {}", request.getCoupleId());

        // 1. Find initiator user
        User initiator = userRepository.findByUserId(initiatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", initiatorUserId));

        // 2. Find couple
        Couple couple = coupleRepository.findByCoupleId(request.getCoupleId())
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", request.getCoupleId()));

        // 3. Verify couple is ACTIVE
        if (!"ACTIVE".equals(couple.getStatus())) {
            throw new InvalidOperationException("Cannot create restriction for couple with status: " + couple.getStatus());
        }

        // 4. Verify initiator is part of this couple
        if (!isUserPartOfCouple(initiator, couple)) {
            throw new InvalidOperationException("User is not part of this couple");
        }

        // 5. Verify couple PIN
        if (!coupleService.verifyCouplePin(request.getCoupleId(), request.getCouplePin())) {
            log.warn("Invalid couple PIN attempt for restriction creation");
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 6. Validate time-bound dates
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        
        if (endDate.isBefore(startDate)) {
            throw new InvalidOperationException("End date cannot be before start date");
        }
        
        if (endDate.isBefore(LocalDate.now())) {
            throw new InvalidOperationException("End date cannot be in the past");
        }

        // 7. Generate unique Restriction ID
        String restrictionId = IdGenerator.generateRestrictionId();
        while (restrictionRepository.existsByRestrictionId(restrictionId)) {
            restrictionId = IdGenerator.generateRestrictionId();
        }

        // 8. Determine which user is creating
        boolean isUser1 = couple.getUser1().getId().equals(initiator.getId());

        // 9. Create restriction entity
        Restriction restriction = new Restriction();
        restriction.setRestrictionId(restrictionId);
        restriction.setCouple(couple);
        restriction.setDescription(request.getDescription());
        restriction.setStatus("PENDING"); // Awaiting partner approval
        restriction.setStartDate(startDate);
        restriction.setEndDate(endDate);
        restriction.setCategory(request.getCategory());
        restriction.setSeverity(request.getSeverity());
        //implement later
        // restriction.setAutoRenew(request.isAutoRenew());
        // restriction.setCreatedAt(LocalDateTime.now());
        // restriction.setUpdatedAt(LocalDateTime.now());

        // Creator is auto-approved
        if (isUser1) {
            restriction.setUser1Approved(true);
            restriction.setUser2Approved(false);
        } else {
            restriction.setUser1Approved(false);
            restriction.setUser2Approved(true);
        }

        // 10. Save restriction
        Restriction savedRestriction = restrictionRepository.save(restriction);
        log.info("Restriction created with ID: {}. Status: PENDING", savedRestriction.getRestrictionId());

        return restrictionMapper.toResponse(savedRestriction);
    }

    @Override
    @Transactional
    public RestrictionResponse approveRestriction(String partnerUserId, RestrictionApprovalRequestDto request) {
        log.info("Partner {} responding to restriction {}", partnerUserId, request.getRestrictionId());

        // 1. Find the restriction
        Restriction restriction = restrictionRepository.findByRestrictionId(request.getRestrictionId())
                .orElseThrow(() -> new ResourceNotFoundException("Restriction", "restrictionId", request.getRestrictionId()));

        // 2. Verify restriction is PENDING
        if (!"PENDING".equals(restriction.getStatus())) {
            throw new InvalidOperationException("Restriction has already been processed");
        }

        // 3. Find partner user
        User partner = userRepository.findByUserId(partnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", partnerUserId));

        // 4. Verify partner is part of this couple
        Couple couple = restriction.getCouple();
        if (!isUserPartOfCouple(partner, couple)) {
            throw new InvalidOperationException("User is not authorized to approve this restriction");
        }

        // 5. Verify Couple PIN
        if (!coupleService.verifyCouplePin(couple.getCoupleId(), request.getCouplePin())) {
            log.warn("Invalid couple PIN attempt for restriction approval");
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 6. Determine which user is responding
        boolean isUser1 = couple.getUser1().getId().equals(partner.getId());

        // 7. Check if this user hasn't already approved
        if ((isUser1 && restriction.isUser1Approved()) || (!isUser1 && restriction.isUser2Approved())) {
            throw new InvalidOperationException("You have already approved this restriction");
        }

        // 8. Handle approval or rejection
        if (request.getApproved()) {
            // APPROVED
            if (isUser1) {
                restriction.setUser1Approved(true);
            } else {
                restriction.setUser2Approved(true);
            }

            // If both approved, activate the restriction
            if (restriction.isUser1Approved() && restriction.isUser2Approved()) {
                restriction.setStatus("ACTIVE");
                log.info("Restriction {} activated (both partners approved)", restriction.getRestrictionId());
            }

            // restriction.setUpdatedAt(LocalDateTime.now());
            Restriction approvedRestriction = restrictionRepository.save(restriction);

            return restrictionMapper.toResponse(approvedRestriction);
        } else {
            // REJECTED
            String restrictionId = restriction.getRestrictionId();
            restrictionRepository.delete(restriction);
            log.info("Restriction {} rejected and deleted", restrictionId);

            return null;
        }
    }

    @Override
    public RestrictionResponse getRestrictionById(String restrictionId) {
        log.info("Fetching restriction by ID: {}", restrictionId);

        Restriction restriction = restrictionRepository.findByRestrictionId(restrictionId)
                .orElseThrow(() -> new ResourceNotFoundException("Restriction", "restrictionId", restrictionId));

        return restrictionMapper.toResponse(restriction);
    }

    @Override
    public List<RestrictionResponse> getRestrictionsByCouple(String coupleId) {
        log.info("Fetching all restrictions for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return restrictionRepository.findByCoupleOrderByCreatedDateDesc(couple).stream()
                .map(restrictionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestrictionResponse> getActiveRestrictionsByCouple(String coupleId) {
        log.info("Fetching active restrictions for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return restrictionRepository.findActiveRestrictionsByCouple(couple).stream()
                .map(restrictionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestrictionResponse> getPendingRestrictionsByCouple(String coupleId) {
        log.info("Fetching pending restrictions for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return restrictionRepository.findPendingRestrictionsByCouple(couple).stream()
                .map(restrictionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestrictionResponse> getExpiringSoonRestrictions(String coupleId) {
        log.info("Fetching expiring soon restrictions for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        return restrictionRepository.findExpiringSoon(couple, today, sevenDaysLater).stream()
                .map(restrictionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestrictionResponse updateRestriction(RestrictionUpdateRequestDto request) {
        log.info("Updating restriction: {}", request.getRestrictionId());

        // 1. Find restriction
        Restriction restriction = restrictionRepository.findByRestrictionId(request.getRestrictionId())
                .orElseThrow(() -> new ResourceNotFoundException("Restriction", "restrictionId", request.getRestrictionId()));

        // 2. Verify couple PIN
        String coupleId = restriction.getCouple().getCoupleId();
        if (!coupleService.verifyCouplePin(coupleId, request.getCouplePin())) {
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 3. Verify restriction is ACTIVE
        if (!"ACTIVE".equals(restriction.getStatus())) {
            throw new InvalidOperationException("Only ACTIVE restrictions can be updated");
        }

        // 4. Update end date if provided (extension)
        if (request.getNewEndDate() != null) {
            LocalDate newEndDate = request.getNewEndDate();
            
            if (newEndDate.isBefore(restriction.getStartDate())) {
                throw new InvalidOperationException("New end date cannot be before start date");
            }
            
            restriction.setEndDate(newEndDate);
            log.info("Restriction {} end date extended to {}", restriction.getRestrictionId(), newEndDate);
        }

        // 5. Update description if provided
        if (request.getNewDescription() != null && !request.getNewDescription().trim().isEmpty()) {
            restriction.setDescription(request.getNewDescription());
        }

        // 6. Update status if provided (for revocation)
        if (request.getNewStatus() != null && "REVOKED".equals(request.getNewStatus().toUpperCase())) {
            restriction.setStatus("REVOKED");
            log.info("Restriction {} revoked early", restriction.getRestrictionId());
        }

        // restriction.setUpdatedAt(LocalDateTime.now());
        Restriction updatedRestriction = restrictionRepository.save(restriction);

        return restrictionMapper.toResponse(updatedRestriction);
    }

    @Override
    @Transactional
    public RestrictionResponse revokeRestriction(String restrictionId, String couplePin) {
        log.info("Revoking restriction: {}", restrictionId);

        Restriction restriction = restrictionRepository.findByRestrictionId(restrictionId)
                .orElseThrow(() -> new ResourceNotFoundException("Restriction", "restrictionId", restrictionId));

        // Verify couple PIN
        if (!coupleService.verifyCouplePin(restriction.getCouple().getCoupleId(), couplePin)) {
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // Only ACTIVE restrictions can be revoked
        if (!"ACTIVE".equals(restriction.getStatus())) {
            throw new InvalidOperationException("Only ACTIVE restrictions can be revoked");
        }

        restriction.setStatus("REVOKED");
        // restriction.setUpdatedAt(LocalDateTime.now());
        Restriction revokedRestriction = restrictionRepository.save(restriction);

        log.info("Restriction {} revoked successfully", restrictionId);
        return restrictionMapper.toResponse(revokedRestriction);
    }

    @Override
    @Transactional
    public void deleteRestriction(String restrictionId, String couplePin) {
        log.info("Deleting restriction: {}", restrictionId);

        Restriction restriction = restrictionRepository.findByRestrictionId(restrictionId)
                .orElseThrow(() -> new ResourceNotFoundException("Restriction", "restrictionId", restrictionId));

        // Verify couple PIN
        if (!coupleService.verifyCouplePin(restriction.getCouple().getCoupleId(), couplePin)) {
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // Only PENDING restrictions can be deleted
        if (!"PENDING".equals(restriction.getStatus())) {
            throw new InvalidOperationException("Only PENDING restrictions can be deleted. Use revoke for active restrictions.");
        }

        restrictionRepository.delete(restriction);
        log.info("Restriction {} deleted successfully", restrictionId);
    }

    @Override
    public List<RestrictionResponse> getRestrictionsByStatus(String coupleId, String status) {
        log.info("Fetching {} restrictions for couple: {}", status, coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return restrictionRepository.findByCoupleAndStatus(couple, status).stream()
                .map(restrictionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int expireOverdueRestrictions() {
        log.info("Running auto-expiry check for overdue restrictions");

        List<Restriction> expiredRestrictions = restrictionRepository.findExpiredRestrictions(LocalDate.now());

        for (Restriction restriction : expiredRestrictions) {
            restriction.setStatus("EXPIRED");
            // restriction.setUpdatedAt(LocalDateTime.now());
            restrictionRepository.save(restriction);
            
            log.info("Auto-expired restriction: {}", restriction.getRestrictionId());
        }

        log.info("Auto-expired {} restriction(s)", expiredRestrictions.size());
        return expiredRestrictions.size();
    }

    @Override
    public List<RestrictionResponse> getRestrictionsBySeverity(String coupleId, String severity) {
        log.info("Fetching {} severity restrictions for couple: {}", severity, coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return restrictionRepository.findByCoupleAndSeverity(couple, severity).stream()
                .map(restrictionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to check if user is part of couple
     */
    private boolean isUserPartOfCouple(User user, Couple couple) {
        return couple.getUser1().getId().equals(user.getId()) || 
               couple.getUser2().getId().equals(user.getId());
    }
}