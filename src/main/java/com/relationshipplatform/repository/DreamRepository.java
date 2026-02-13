package com.relationshipplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.Dream;

@Repository
public interface DreamRepository extends JpaRepository <Dream, Long>{
    
    /**
     * Find dream by public dream ID
     */
    Optional<Dream> findByDreamId(String dreamId);

    /**
     * Check if dream exists by dream ID
     */
    boolean existsByDreamId(String dreamId);

    /**
     * Find all dreams for a couple
     */
    List<Dream> findByCouple(Couple couple);

    /**
     * Find dreams by couple and status
     * Status: ACTIVE, ACHIEVED, ABANDONED
     */
    List<Dream> findByCoupleAndStatus(Couple couple, String status);

    /**
     * Find active dreams for a couple
     */
    @Query("SELECT d FROM Dream d WHERE d.couple = :couple AND d.status = 'ACTIVE'")
    List<Dream> findActiveDreamsByCouple(@Param("couple") Couple couple);

    /**
     * Find achieved dreams for a couple
     */
    @Query("SELECT d FROM Dream d WHERE d.couple = :couple AND d.status = 'ACHIEVED' ORDER BY d.achievedAt DESC")
    List<Dream> findAchievedDreamsByCouple(@Param("couple") Couple couple);

    /**
     * Count active dreams for a couple
     */
    @Query("SELECT COUNT(d) FROM Dream d WHERE d.couple.id = :coupleId AND d.status = 'ACTIVE'")
    int countActiveByCoupleId(@Param("coupleId") Long coupleId);

    /**
     * Count achieved dreams for a couple
     */
    @Query("SELECT COUNT(d) FROM Dream d WHERE d.couple = :couple AND d.status = 'ACHIEVED'")
    long countAchievedByCouple(@Param("couple") Couple couple);

    /**
     * Find all dreams ordered by creation date
     */
    @Query("SELECT d FROM Dream d WHERE d.couple = :couple ORDER BY d.createdAt DESC")
    List<Dream> findByCoupleOrderByCreatedDateDesc(@Param("couple") Couple couple);

    /**
     * Find dreams by category
     */
    @Query("SELECT d FROM Dream d WHERE d.couple = :couple AND d.category = :category")
    List<Dream> findByCoupleAndCategory(@Param("couple") Couple couple, @Param("category") String category);
}
