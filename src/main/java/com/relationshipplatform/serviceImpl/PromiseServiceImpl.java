package com.relationshipplatform.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.relationshipplatform.dto.request.promise.PromiseApprovalRequestDto;
import com.relationshipplatform.dto.request.promise.PromiseCreateRequestDto;
import com.relationshipplatform.dto.request.promise.PromiseUpdateRequestDto;
import com.relationshipplatform.dto.response.promise.PromiseResponse;
import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.Promise;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.exception.AuthenticationException;
import com.relationshipplatform.exception.InvalidOperationException;
import com.relationshipplatform.exception.ResourceNotFoundException;
import com.relationshipplatform.mapper.PromiseMapper;
import com.relationshipplatform.repository.CoupleRepository;
import com.relationshipplatform.repository.PromiseRepository;
import com.relationshipplatform.repository.UserRepository;
import com.relationshipplatform.service.CoupleService;
import com.relationshipplatform.service.PromiseService;
import com.relationshipplatform.utility.IdGenerator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PromiseServiceImpl implements PromiseService{


    private final PromiseRepository promiseRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final CoupleService coupleService;
    private final PromiseMapper promiseMapper;

    public PromiseServiceImpl(PromiseRepository promiseRepository,
                              CoupleRepository coupleRepository,
                              UserRepository userRepository,
                              CoupleService coupleService,
                              PromiseMapper promiseMapper) {
        this.promiseRepository = promiseRepository;
        this.coupleRepository = coupleRepository;
        this.userRepository = userRepository;
        this.coupleService = coupleService;
        this.promiseMapper = promiseMapper;
    }


    @Override
    @Transactional
    public PromiseResponse createPromise(String initiatorUserId, PromiseCreateRequestDto request) {
        log.info("Creating promise for couple: {}", request.getCoupleId());

        // 1. Find initiator user
        User initiator = userRepository.findByUserId(initiatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", initiatorUserId));

        // 2. Find couple
        Couple couple = coupleRepository.findByCoupleId(request.getCoupleId())
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", request.getCoupleId()));

        // 3. Verify couple is ACTIVE
        if (!"ACTIVE".equals(couple.getStatus())) {
            throw new InvalidOperationException("Cannot create promise for couple with status: " + couple.getStatus());
        }

        // 4. Verify initiator is part of this couple
        if (!isUserPartOfCouple(initiator, couple)) {
            throw new InvalidOperationException("User is not part of this couple");
        }

        // 5. Verify couple PIN
        if (!coupleService.verifyCouplePin(request.getCoupleId(), request.getCouplePin())) {
            log.warn("Invalid couple PIN attempt for promise creation");
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 6. Generate unique Promise ID
        String promiseId = IdGenerator.generatePromiseId();
        while (promiseRepository.existsByPromiseId(promiseId)) {
            promiseId = IdGenerator.generatePromiseId();
        }

        // 7. Determine which user is creating (user1 or user2)
        boolean isUser1 = couple.getUser1().getId().equals(initiator.getId());

        // 8. Create promise entity
        Promise promise = new Promise();
        promise.setPromiseId(promiseId);
        promise.setCouple(couple);
        promise.setDescription(request.getDescription());
        promise.setStatus("PENDING"); // Awaiting partner approval
        // Parse expiry date if provided
        if (request.getExpiryDate() != null && !request.getExpiryDate().trim().isEmpty()) {
            try {
                promise.setExpiryDate(LocalDate.parse(request.getExpiryDate()));
            } catch (Exception e) {
                log.warn("Invalid expiry date format: {}", request.getExpiryDate());
            }
        }
        
        promise.setCreatedAt(LocalDateTime.now());
        promise.setUpdatedAt(LocalDateTime.now());

        // Creator is auto-approved
        if (isUser1) {
            promise.setUser1Approved(true);
            promise.setUser2Approved(false);
        } else {
            promise.setUser1Approved(false);
            promise.setUser2Approved(true);
        }

        // 9. Save promise
        Promise savedPromise = promiseRepository.save(promise);
        log.info("Promise created with ID: {}. Status: PENDING", savedPromise.getPromiseId());

        // TODO: Send notification to partner about new promise

        return promiseMapper.toResponse(savedPromise);
    }

    @Override
    @Transactional
    public PromiseResponse approvePromise(String partnerUserId, PromiseApprovalRequestDto request) {
        log.info("Partner {} responding to promise {}", partnerUserId, request.getPromiseId());

        // 1. Find the promise
        Promise promise = promiseRepository.findByPromiseId(request.getPromiseId())
                .orElseThrow(() -> new ResourceNotFoundException("Promise", "promiseId", request.getPromiseId()));

        // 2. Verify promise is PENDING
        if (!"PENDING".equals(promise.getStatus())) {
            throw new InvalidOperationException("Promise has already been processed");
        }

        // 3. Find partner user
        User partner = userRepository.findByUserId(partnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", partnerUserId));

        // 4. Verify partner is part of this couple
        Couple couple = promise.getCouple();
        if (!isUserPartOfCouple(partner, couple)) {
            throw new InvalidOperationException("User is not authorized to approve this promise");
        }

        // 5. Verify Couple PIN
        if (!coupleService.verifyCouplePin(couple.getCoupleId(), request.getCouplePin())) {
            log.warn("Invalid couple PIN attempt for promise approval");
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 6. Determine which user is responding
        boolean isUser1 = couple.getUser1().getId().equals(partner.getId());

        // 7. Check if this user hasn't already approved
        if ((isUser1 && promise.isUser1Approved()) || (!isUser1 && promise.isUser2Approved())) {
            throw new InvalidOperationException("You have already approved this promise");
        }

        // 8. Handle approval or rejection
        if (request.getApproved()) {
            // APPROVED
            if (isUser1) {
                promise.setUser1Approved(true);
            } else {
                promise.setUser2Approved(true);
            }

            // If both approved, activate the promise
            if (promise.isUser1Approved() && promise.isUser2Approved()) {
                promise.setStatus("ACTIVE");
                log.info("Promise {} activated (both partners approved)", promise.getPromiseId());
            }

            promise.setUpdatedAt(LocalDateTime.now());
            Promise approvedPromise = promiseRepository.save(promise);

            // TODO: Send notification to initiator about approval

            return promiseMapper.toResponse(approvedPromise);
        } else {
            // REJECTED
            String promiseId = promise.getPromiseId();
            promiseRepository.delete(promise);
            log.info("Promise {} rejected and deleted", promiseId);

            // TODO: Send notification to initiator about rejection

            return null;
        }
    }

    @Override
    public PromiseResponse getPromiseById(String promiseId) {
        log.info("Fetching promise by ID: {}", promiseId);

        Promise promise = promiseRepository.findByPromiseId(promiseId)
                .orElseThrow(() -> new ResourceNotFoundException("Promise", "promiseId", promiseId));

        return promiseMapper.toResponse(promise);
    }

    @Override
    public List<PromiseResponse> getPromisesByCouple(String coupleId) {
        log.info("Fetching all promises for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return promiseRepository.findByCoupleOrderByCreatedDateDesc(couple).stream()
                .map(promiseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromiseResponse> getActivePromisesByCouple(String coupleId) {
        log.info("Fetching active promises for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return promiseRepository.findActivePromisesByCouple(couple).stream()
                .map(promiseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromiseResponse> getPendingPromisesByCouple(String coupleId) {
        log.info("Fetching pending promises for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return promiseRepository.findPendingPromisesByCouple(couple).stream()
                .map(promiseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PromiseResponse updatePromiseStatus(PromiseUpdateRequestDto request) {
        log.info("Updating promise status: {}", request.getPromiseId());

        // 1. Find promise
        Promise promise = promiseRepository.findByPromiseId(request.getPromiseId())
                .orElseThrow(() -> new ResourceNotFoundException("Promise", "promiseId", request.getPromiseId()));

        // 2. Verify couple PIN
        String coupleId = promise.getCouple().getCoupleId();
        if (!coupleService.verifyCouplePin(coupleId, request.getCouplePin())) {
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 3. Verify promise is ACTIVE
        if (!"ACTIVE".equals(promise.getStatus())) {
            throw new InvalidOperationException("Only ACTIVE promises can be updated");
        }

        // 4. Update status
        if (request.getNewStatus() != null) {
            String newStatus = request.getNewStatus().toUpperCase();

            if ("COMPLETED".equals(newStatus)) {
                promise.setStatus("COMPLETED");
                log.info("Promise {} marked as COMPLETED", promise.getPromiseId());
                
                // Increase relationship health by 5
                int currentHealth = promise.getCouple().getRelationshipHealth();
                int newHealth = Math.min(100, currentHealth + 5);
                coupleService.updateRelationshipHealth(coupleId, newHealth);
                log.info("Relationship health increased from {} to {} (promise completed)", currentHealth, newHealth);
                
            } else if ("BROKEN".equals(newStatus)) {
                promise.setStatus("BROKEN");
                log.warn("Promise {} marked as BROKEN", promise.getPromiseId());
                
                // Decrease relationship health by 10
                int currentHealth = promise.getCouple().getRelationshipHealth();
                int newHealth = Math.max(0, currentHealth - 10);
                coupleService.updateRelationshipHealth(coupleId, newHealth);
                log.info("Relationship health decreased from {} to {} (promise broken)", currentHealth, newHealth);
            }
        }

        // 5. Update description if provided
        if (request.getNewDescription() != null && !request.getNewDescription().trim().isEmpty()) {
            promise.setDescription(request.getNewDescription());
        }

        promise.setUpdatedAt(LocalDateTime.now());
        Promise updatedPromise = promiseRepository.save(promise);

        return promiseMapper.toResponse(updatedPromise);
    }

    @Override
    @Transactional
    public void deletePromise(String promiseId, String couplePin) {
        log.info("Deleting promise: {}", promiseId);

        Promise promise = promiseRepository.findByPromiseId(promiseId)
                .orElseThrow(() -> new ResourceNotFoundException("Promise", "promiseId", promiseId));

        // Verify couple PIN
        if (!coupleService.verifyCouplePin(promise.getCouple().getCoupleId(), couplePin)) {
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // Only PENDING or ACTIVE promises can be deleted
        if ("COMPLETED".equals(promise.getStatus()) || "BROKEN".equals(promise.getStatus())) {
            throw new InvalidOperationException("Cannot delete completed or broken promises");
        }

        promiseRepository.delete(promise);
        log.info("Promise {} deleted successfully", promiseId);
    }

    @Override
    public List<PromiseResponse> getPromisesByStatus(String coupleId, String status) {
        log.info("Fetching {} promises for couple: {}", status, coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return promiseRepository.findByCoupleAndStatus(couple, status).stream()
                .map(promiseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getCompletedPromisesCount(String coupleId) {
        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return promiseRepository.countCompletedByCouple(couple);
    }

    @Override
    public long getBrokenPromisesCount(String coupleId) {
        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return promiseRepository.countBrokenByCouple(couple);
    }

    /**
     * Helper method to check if user is part of couple
     */
    private boolean isUserPartOfCouple(User user, Couple couple) {
        return couple.getUser1().getId().equals(user.getId()) || 
               couple.getUser2().getId().equals(user.getId());
    }
}
