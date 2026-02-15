package com.relationshipplatform.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.relationshipplatform.dto.request.dream.DreamCreateRequestDto;
import com.relationshipplatform.dto.request.dream.DreamUpdateRequestDto;
import com.relationshipplatform.dto.response.dream.DreamResponse;
import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.Dream;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.exception.AuthenticationException;
import com.relationshipplatform.exception.InvalidOperationException;
import com.relationshipplatform.exception.ResourceNotFoundException;
import com.relationshipplatform.mapper.DreamMapper;
import com.relationshipplatform.repository.CoupleRepository;
import com.relationshipplatform.repository.DreamRepository;
import com.relationshipplatform.repository.UserRepository;
import com.relationshipplatform.service.CoupleService;
import com.relationshipplatform.service.DreamService;
import com.relationshipplatform.utility.IdGenerator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DreamServiceImpl implements DreamService {

    private final DreamRepository dreamRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final CoupleService coupleService;
    private final DreamMapper dreamMapper;

    public DreamServiceImpl(DreamRepository dreamRepository,
                            CoupleRepository coupleRepository,
                            UserRepository userRepository,
                            CoupleService coupleService,
                            DreamMapper dreamMapper) {
        this.dreamRepository = dreamRepository;
        this.coupleRepository = coupleRepository;
        this.userRepository = userRepository;
        this.coupleService = coupleService;
        this.dreamMapper = dreamMapper;
    }

    @Override
    @Transactional
    public DreamResponse createDream(String creatorUserId, DreamCreateRequestDto request) {
        log.info("Creating dream for couple: {}", request.getCoupleId());

        // 1. Find creator user
        User creator = userRepository.findByUserId(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", creatorUserId));

        // 2. Find couple
        Couple couple = coupleRepository.findByCoupleId(request.getCoupleId())
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", request.getCoupleId()));

        // 3. Verify couple is ACTIVE
        if (!"ACTIVE".equals(couple.getStatus())) {
            throw new InvalidOperationException("Cannot create dream for couple with status: " + couple.getStatus());
        }

        // 4. Verify creator is part of this couple
        if (!isUserPartOfCouple(creator, couple)) {
            throw new InvalidOperationException("User is not part of this couple");
        }

        // 5. Verify couple PIN
        if (!coupleService.verifyCouplePin(request.getCoupleId(), request.getCouplePin())) {
            log.warn("Invalid couple PIN attempt for dream creation");
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // 6. Generate unique Dream ID
        String dreamId = IdGenerator.generateDreamId();
        while (dreamRepository.existsByDreamId(dreamId)) {
            dreamId = IdGenerator.generateDreamId();
        }

        // 7. Create dream entity
        Dream dream = new Dream();
        dream.setDreamId(dreamId);
        dream.setCouple(couple);
        dream.setDescription(request.getDescription());
        dream.setStatus("ACTIVE"); // Dreams are immediately ACTIVE (no approval needed)
        dream.setCategory(request.getCategory());
        
        // Parse target date if provided
        if (request.getTargetDate() != null && !request.getTargetDate().trim().isEmpty()) {
            try {
                dream.setTargetDate(LocalDate.parse(request.getTargetDate()));
            } catch (Exception e) {
                log.warn("Invalid target date format: {}", request.getTargetDate());
            }
        }
        
        dream.setCreatedAt(LocalDateTime.now());
        dream.setUpdatedAt(LocalDateTime.now());

        // 8. Save dream
        Dream savedDream = dreamRepository.save(dream);
        log.info("Dream created with ID: {}. Status: ACTIVE (no approval needed)", savedDream.getDreamId());

        return dreamMapper.toResponse(savedDream);
    }

    @Override
    public DreamResponse getDreamById(String dreamId) {
        log.info("Fetching dream by ID: {}", dreamId);

        Dream dream = dreamRepository.findByDreamId(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("Dream", "dreamId", dreamId));

        return dreamMapper.toResponse(dream);
    }

    @Override
    public List<DreamResponse> getDreamsByCouple(String coupleId) {
        log.info("Fetching all dreams for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return dreamRepository.findByCoupleOrderByCreatedDateDesc(couple).stream()
                .map(dreamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DreamResponse> getActiveDreamsByCouple(String coupleId) {
        log.info("Fetching active dreams for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return dreamRepository.findActiveDreamsByCouple(couple).stream()
                .map(dreamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DreamResponse> getAchievedDreamsByCouple(String coupleId) {
        log.info("Fetching achieved dreams for couple: {}", coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return dreamRepository.findAchievedDreamsByCouple(couple).stream()
                .map(dreamMapper::toResponse)
                .collect(Collectors.toList());
    }

    // @Override
    // @Transactional
    // public DreamResponse updateDreamStatus(DreamUpdateRequestDto request) {
    //     log.info("Updating dream status: {}", request.getDreamId());

    //     // 1. Find dream
    //     Dream dream = dreamRepository.findByDreamId(request.getDreamId())
    //             .orElseThrow(() -> new ResourceNotFoundException("Dream", "dreamId", request.getDreamId()));

    //     // 2. Verify couple PIN
    //     String coupleId = dream.getCouple().getCoupleId();
    //     if (!coupleService.verifyCouplePin(coupleId, request.getCouplePin())) {
    //         throw new AuthenticationException("Invalid Couple PIN");
    //     }

    //     // 3. Verify dream is ACTIVE
    //     if (!"ACTIVE".equals(dream.getStatus())) {
    //         throw new InvalidOperationException("Only ACTIVE dreams can be updated");
    //     }

    //     // 4. Update status
    //     if (request.getNewStatus() != null) {
    //         String newStatus = request.getNewStatus().toUpperCase();

    //         if ("ACHIEVED".equals(newStatus)) {
    //             dream.setStatus("ACHIEVED");
    //             dream.setAchievedAt(LocalDateTime.now());
                
    //             // Set achievement notes if provided
    //             if (request.getAchievementNotes() != null) {
    //                 dream.setAchievementNotes(request.getAchievementNotes());
    //             }
                
    //             // Parse achievement date if provided (custom date)
    //             if (request.getAchievementDate() != null && !request.getAchievementDate().trim().isEmpty()) {
    //                 try {
    //                     LocalDate achievementDate = LocalDate.parse(request.getAchievementDate());
    //                     dream.setAchievedAt(achievementDate.atStartOfDay());
    //                 } catch (Exception e) {
    //                     log.warn("Invalid achievement date format: {}", request.getAchievementDate());
    //                 }
    //             }
                
    //             log.info("Dream {} marked as ACHIEVED", dream.getDreamId());
                
    //             // Increase relationship health by 5
    //             int currentHealth = dream.getCouple().getRelationshipHealth();
    //             int newHealth = Math.min(100, currentHealth + 5);
    //             coupleService.updateRelationshipHealth(coupleId, newHealth);
    //             log.info("Relationship health increased from {} to {} (dream achieved)", currentHealth, newHealth);
                
    //         } else if ("ABANDONED".equals(newStatus)) {
    //             dream.setStatus("ABANDONED");
    //             log.info("Dream {} marked as ABANDONED", dream.getDreamId());
    //         }
    //     }

    //     // 5. Update description if provided
    //     if (request.getNewDescription() != null && !request.getNewDescription().trim().isEmpty()) {
    //         dream.setDescription(request.getNewDescription());
    //     }

    //     dream.setUpdatedAt(LocalDateTime.now());
    //     Dream updatedDream = dreamRepository.save(dream);

    //     return dreamMapper.toResponse(updatedDream);
    // }

    @Override
    @Transactional
    public void deleteDream(String dreamId, String couplePin) {
        log.info("Deleting dream: {}", dreamId);

        Dream dream = dreamRepository.findByDreamId(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("Dream", "dreamId", dreamId));

        // Verify couple PIN
        if (!coupleService.verifyCouplePin(dream.getCouple().getCoupleId(), couplePin)) {
            throw new AuthenticationException("Invalid Couple PIN");
        }

        // Only ACTIVE dreams can be deleted
        if (!"ACTIVE".equals(dream.getStatus())) {
            throw new InvalidOperationException("Only ACTIVE dreams can be deleted");
        }

        dreamRepository.delete(dream);
        log.info("Dream {} deleted successfully", dreamId);
    }

    @Override
    public List<DreamResponse> getDreamsByStatus(String coupleId, String status) {
        log.info("Fetching {} dreams for couple: {}", status, coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return dreamRepository.findByCoupleAndStatus(couple, status).stream()
                .map(dreamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DreamResponse> getDreamsByCategory(String coupleId, String category) {
        log.info("Fetching dreams in category {} for couple: {}", category, coupleId);

        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return dreamRepository.findByCoupleAndCategory(couple, category).stream()
                .map(dreamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getAchievedDreamsCount(String coupleId) {
        Couple couple = coupleRepository.findByCoupleId(coupleId)
                .orElseThrow(() -> new ResourceNotFoundException("Couple", "coupleId", coupleId));

        return dreamRepository.countAchievedByCouple(couple);
    }

    /**
     * Helper method to check if user is part of couple
     */
    private boolean isUserPartOfCouple(User user, Couple couple) {
        return couple.getUser1().getId().equals(user.getId()) || 
               couple.getUser2().getId().equals(user.getId());
    }
    
}
