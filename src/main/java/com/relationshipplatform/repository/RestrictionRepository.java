package com.relationshipplatform.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.Restriction;

/**
 * Repository for Restriction entity
 * Handles time-bound restriction data access operations
 */
@Repository
public interface RestrictionRepository extends JpaRepository<Restriction, Long> {

    /**
     * Find restriction by public restriction ID
     */
    Optional<Restriction> findByRestrictionId(String restrictionId);

    /**
     * Check if restriction exists by restriction ID
     */
    boolean existsByRestrictionId(String restrictionId);

    /**
     * Find all restrictions for a couple
     */
    List<Restriction> findByCouple(Couple couple);

    /**
     * Find restrictions by couple and status
     * Status: PENDING, ACTIVE, EXPIRED, REVOKED
     */
    List<Restriction> findByCoupleAndStatus(Couple couple, String status);

    /**
     * Find active restrictions for a couple
     */
    @Query("SELECT r FROM Restriction r WHERE r.couple = :couple AND r.status = 'ACTIVE' ORDER BY r.endDate ASC")
    List<Restriction> findActiveRestrictionsByCouple(@Param("couple") Couple couple);

    /**
     * Find pending restrictions (awaiting approval)
     */
    @Query("SELECT r FROM Restriction r WHERE r.couple = :couple AND r.status = 'PENDING'")
    List<Restriction> findPendingRestrictionsByCouple(@Param("couple") Couple couple);

    /**
     * Find expired restrictions that need status update
     * Used by scheduler to auto-expire
     */
    @Query("SELECT r FROM Restriction r WHERE r.status = 'ACTIVE' AND r.endDate < :currentDate")
    List<Restriction> findExpiredRestrictions(@Param("currentDate") LocalDate currentDate);

    /**
     * Find restrictions expiring soon (within days)
     */
    @Query("SELECT r FROM Restriction r WHERE r.couple = :couple AND r.status = 'ACTIVE' " +
           "AND r.endDate BETWEEN :today AND :futureDate")
    List<Restriction> findExpiringSoon(@Param("couple") Couple couple, 
                                      @Param("today") LocalDate today,
                                      @Param("futureDate") LocalDate futureDate);

    /**
     * Count active restrictions for a couple
     */
    @Query("SELECT COUNT(r) FROM Restriction r WHERE r.couple.id = :coupleId AND r.status = 'ACTIVE'")
    int countActiveByCoupleId(@Param("coupleId") Long coupleId);

    /**
     * Count pending approvals for specific user
     */
    @Query("SELECT COUNT(r) FROM Restriction r WHERE r.couple.id = :coupleId " +
           "AND r.status = 'PENDING' " +
           "AND (:isUser1 = true AND r.user1Approved = false " +
           "OR :isUser1 = false AND r.user2Approved = false)")
    int countPendingApprovalsForUser(@Param("coupleId") Long coupleId, 
                                     @Param("isUser1") boolean isUser1);

    /**
     * Find restrictions by severity
     */
    @Query("SELECT r FROM Restriction r WHERE r.couple = :couple AND r.severity = :severity AND r.status = 'ACTIVE'")
    List<Restriction> findByCoupleAndSeverity(@Param("couple") Couple couple, @Param("severity") String severity);

    /**
     * Find restrictions by category
     */
    @Query("SELECT r FROM Restriction r WHERE r.couple = :couple AND r.category = :category")
    List<Restriction> findByCoupleAndCategory(@Param("couple") Couple couple, @Param("category") String category);

    /**
     * Find all restrictions ordered by creation date
     */
    @Query("SELECT r FROM Restriction r WHERE r.couple = :couple ORDER BY r.createdAt DESC")
    List<Restriction> findByCoupleOrderByCreatedDateDesc(@Param("couple") Couple couple);

    /**
     * Count revoked restrictions (measure of flexibility)
     */
    @Query("SELECT COUNT(r) FROM Restriction r WHERE r.couple = :couple AND r.status = 'REVOKED'")
    long countRevokedByCouple(@Param("couple") Couple couple);
}












