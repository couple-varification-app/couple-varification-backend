package com.relationshipplatform.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.relationshipplatform.dto.request.couple.CoupleAcceptRequestDto;
import com.relationshipplatform.dto.request.couple.CoupleCreateRequestDto;
import com.relationshipplatform.dto.request.couple.CoupleUpdateRequestDto;
import com.relationshipplatform.dto.response.couple.CoupleResponse;
import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.RelationshipHealth;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.exception.AgeVerificationException;
import com.relationshipplatform.exception.AuthenticationException;
import com.relationshipplatform.exception.DuplicateResourceException;
import com.relationshipplatform.exception.InvalidOperationException;
import com.relationshipplatform.exception.ResourceNotFoundException;
import com.relationshipplatform.mapper.CoupleMapper;
import com.relationshipplatform.repository.CoupleRepository;
import com.relationshipplatform.repository.UserRepository;
import com.relationshipplatform.service.CoupleService;
import com.relationshipplatform.utility.*;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CoupleServiceImpl implements CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final CoupleMapper coupleMapper;
    private final PasswordEncoderUtil passwordEncoder;

    public CoupleServiceImpl(CoupleRepository coupleRepository,
                             UserRepository userRepository,
                             CoupleMapper coupleMapper,
                             PasswordEncoderUtil passwordEncoder) {
        this.coupleRepository = coupleRepository;
        this.userRepository = userRepository;
        this.coupleMapper = coupleMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public CoupleResponse createCoupleRequest(String initiatorUserId, CoupleCreateRequestDto request) {
        log.info("Creating couple request from {} to {}", initiatorUserId, request.getPartnerUserId());

        // 1. Validate PIN and confirm PIN match
        if (!request.getCouplePin().equals(request.getConfirmPin())) {
            throw new InvalidOperationException("Couple PIN and confirm PIN do not match");
        }

        // 2. Find both users
        User initiator = userRepository.findByUserId(initiatorUserId)
                .orElseThrow(() -> {
                    log.warn("Initiator not found: {}", initiatorUserId);
                    return new ResourceNotFoundException("User", "userId", initiatorUserId);
                });

        User partner = userRepository.findByUserId(request.getPartnerUserId())
                .orElseThrow(() -> {
                    log.warn("Partner not found: {}", request.getPartnerUserId());
                    return new ResourceNotFoundException("User", "userId", request.getPartnerUserId());
                });

        // 3. Prevent self-coupling
        if (initiator.getId().equals(partner.getId())) {
            throw new InvalidOperationException("Cannot create couple with yourself");
        }

        // 4. Verify both users are 18+
        if (!AgeCalculator.isEligibleAge(initiator.getDob())) {
            throw new AgeVerificationException("Initiator must be 18 or older");
        }
        if (!AgeCalculator.isEligibleAge(partner.getDob())) {
            throw new AgeVerificationException("Partner must be 18 or older");
        }

        // 5. Check if either user is already in an active couple
        if (userRepository.isUserInActiveCouple(initiator)) {
            throw new InvalidOperationException(
                "You are already in an active relationship. Complete breakup process first."
            );
        }
        if (userRepository.isUserInActiveCouple(partner)) {
            throw new InvalidOperationException(
                "Partner is already in an active relationship"
            );
        }

        // 6. Check if couple already exists between these users
        if (coupleRepository.existsCoupleByUsers(initiator, partner)) {
            throw new DuplicateResourceException(
                "Couple request already exists between these users"
            );
        }

        // 7. Generate unique Couple ID
        String coupleId = IdGenerator.generateCoupleId();
        while (coupleRepository.existsByCoupleId(coupleId)) {
            coupleId = IdGenerator.generateCoupleId();
        }

        // 8. Create couple entity with PENDING status
        Couple couple = new Couple();
        couple.setCoupleId(coupleId);
        couple.setUser1(initiator);
        couple.setUser2(partner);
        couple.setCouplePin(passwordEncoder.encode(request.getCouplePin())); // Encrypt PIN
        couple.setStatus("PENDING"); // Awaiting partner acceptance
        couple.setRelationshipHealth(100); // Will activate when accepted
        couple.setLoyaltyScore(100);

        // 9. Save couple
        Couple savedCouple = coupleRepository.save(couple);
        log.info("Couple request created with ID: {}. Status: PENDING", savedCouple.getCoupleId());

        // TODO: Send notification to partner about couple request

        return coupleMapper.toResponse(savedCouple);
    }

    @Override
    @Transactional
    public CoupleResponse respondToCoupleRequest(String partnerUserId, CoupleAcceptRequestDto request) {
        log.info("Partner {} responding to couple request {}", partnerUserId, request.getCoupleId());

        // 1. Find the couple request
        Couple couple = coupleRepository.findByCoupleId(request.getCoupleId())
                .orElseThrow(() -> {
                    log.warn("Couple not found: {}", request.getCoupleId());
                    return new ResourceNotFoundException("Couple", "coupleId", request.getCoupleId());
                });

        // 2. Verify the couple is in PENDING status
        if (!"PENDING".equals(couple.getStatus())) {
            throw new InvalidOperationException("This couple request has already been processed");
        }

        // 3. Find partner user
        User partner = userRepository.findByUserId(partnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", partnerUserId));

        // 4. Verify this user is the intended partner (user2)
        if (!couple.getUser2().getId().equals(partner.getId())) {
            throw new InvalidOperationException("You are not authorized to respond to this couple request");
        }

        // 5. Verify Couple PIN
        if (!passwordEncoder.matches(request.getCouplePin(), couple.getCouplePin())) {
            log.warn("Invalid couple PIN attempt for couple: {}", request.getCoupleId());
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 6. Handle acceptance or rejection
        if (request.isAccepted()) {
            // ACCEPTED - Activate the couple
            couple.setStatus("ACTIVE");
            
            // Initialize relationship health
            RelationshipHealth health = new RelationshipHealth();
            health.setCouple(couple);
            health.setHealthScore(100);
            health.setLoyaltyScore(100);
            health.setLastUpdated(LocalDateTime.now());
            couple.setRelationshipHealthDetails(health);

            Couple activatedCouple = coupleRepository.save(couple);
            log.info("Couple {} activated successfully", activatedCouple.getCoupleId());

            // TODO: Send notification to initiator about acceptance

            return coupleMapper.toResponse(activatedCouple);
        } else {
            // REJECTED - Delete the couple request
            String coupleId = couple.getCoupleId();
            coupleRepository.delete(couple);
            log.info("Couple request {} rejected and deleted", coupleId);

            // TODO: Send notification to initiator about rejection

            return null; // Or return a rejection response
        }
    }


   @Override
    public CoupleResponse getCoupleById(String coupleId) {
        log.info("Fetching couple by ID: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> {
                    log.warn("Couple not found: {}", coupleId);
                    return new ResourceNotFoundException("Couple", "coupleId", coupleId);
                });

        return coupleMapper.toResponse(couple);
    }


    @Override
    public CoupleResponse getActiveCoupleByUser(String userId) {
        log.info("Fetching active couple for user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        return coupleRepository.findActiveCoupleByUser(user)
                .map(coupleMapper::toResponse)
                .orElse(null);
    }


    @Override
    public CoupleResponse getPendingCoupleRequestForUser(String userId) {
        log.info("Fetching pending couple request for user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        return coupleRepository.findPendingCoupleRequestForUser(user)
                .map(coupleMapper::toResponse)
                .orElse(null);
    }
    @Override
    @Transactional
    public CoupleResponse updateCouple(CoupleUpdateRequestDto request) {
        log.info("Updating couple: {}", request.getCoupleId());

        // 1. Find couple
        Couple couple = coupleRepository.findByCoupleId(request.getCoupleId())
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", request.getCoupleId()));

        // 2. Verify current PIN
        if (!passwordEncoder.matches(request.getCurrentPin(), couple.getCouplePin())) {
            log.warn("Invalid couple PIN attempt for update: {}", request.getCoupleId());
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 3. Update PIN if new PIN provided
        if (request.getNewPin() != null && !request.getNewPin().trim().isEmpty()) {
            // Validate new PIN matches confirm
            if (!request.getNewPin().equals(request.getConfirmNewPin())) {
                throw new InvalidOperationException("New PIN and confirm PIN do not match");
            }
            
            couple.setCouplePin(passwordEncoder.encode(request.getNewPin()));
            log.info("Couple PIN updated for: {}", request.getCoupleId());
        }

        // 4. Update other fields if needed
        // Add more update logic here as needed

        Couple updatedCouple = coupleRepository.save(couple);
        log.info("Couple updated successfully: {}", request.getCoupleId());

        return coupleMapper.toResponse(updatedCouple);
    }

    @Override
    public boolean verifyCouplePin(String coupleId, String pin) {
        log.info("Verifying PIN for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return passwordEncoder.matches(pin, couple.getCouplePin());
    }


    @Override
    @Transactional
    public void updateRelationshipHealth(String coupleId, int newScore) {
        log.info("Updating relationship health for couple {} to {}", coupleId, newScore);

        if (newScore < 0 || newScore > 100) {
            throw new InvalidOperationException("Health score must be between 0 and 100");
        }

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        couple.setRelationshipHealth(newScore);

        if (couple.getRelationshipHealthDetails() != null) {
            couple.getRelationshipHealthDetails().setHealthScore(newScore);
            couple.getRelationshipHealthDetails().setLastUpdated(LocalDateTime.now());
        }

        coupleRepository.save(couple);
        log.info("Relationship health updated successfully");
    }

    @Override
    @Transactional
    public void updateLoyaltyScore(String coupleId, int newScore) {
        log.info("Updating loyalty score for couple {} to {}", coupleId, newScore);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        couple.setLoyaltyScore(newScore);

        if (couple.getRelationshipHealthDetails() != null) {
            couple.getRelationshipHealthDetails().setLoyaltyScore(newScore);
            couple.getRelationshipHealthDetails().setLastUpdated(LocalDateTime.now());
        }

        coupleRepository.save(couple);
        log.info("Loyalty score updated successfully");
    }

    @Override
    public List<CoupleResponse> getAllCouples() {
        log.info("Fetching all couples");

        return coupleRepository.findAll().stream()
                .map(coupleMapper::toResponse)
                .collect(Collectors.toList());
    }
}
