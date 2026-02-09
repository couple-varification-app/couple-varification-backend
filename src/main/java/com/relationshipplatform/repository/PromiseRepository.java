package com.relationshipplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.Promise;

@Repository
public interface PromiseRepository extends JpaRepository <Promise, Long> {
    /**
     * Find promise by public promise ID
     * 
     * @param promiseId Public promise ID (PRM-xxx)
     * @return Promise if found
     */
    Optional<Promise> findByPromiseId(String promiseId);

    /**
     * Check if promise exists by promise ID
     * 
     * @param promiseId Public promise ID
     * @return true if exists
     */
    boolean existsByPromiseId(String promiseId);

    /**
     * Find all promises for a couple
     * 
     * @param couple Couple entity
     * @return List of promises
     */
    List<Promise> findByCouple(Couple couple);

    /**
     * Find all promises for a couple with specific status
     * 
     * @param couple Couple entity
     * @param status Promise status (PENDING, ACTIVE, COMPLETED, BROKEN)
     * @return List of promises
     */
    List<Promise> findByCoupleAndStatus(Couple couple, String status);

    /**
     * Find active promises for a couple
     * 
     * @param couple Couple entity
     * @return List of active promises
     */
    @Query("SELECT p FROM Promise p WHERE p.couple = :couple AND p.status = 'ACTIVE'")
    List<Promise> findActivePromisesByCouple(@Param("couple") Couple couple);

    /**
     * Find pending promises for a couple (awaiting approval)
     * 
     * @param couple Couple entity
     * @return List of pending promises
     */
    @Query("SELECT p FROM Promise p WHERE p.couple = :couple AND p.status = 'PENDING'")
    List<Promise> findPendingPromisesByCouple(@Param("couple") Couple couple);

    /**
     * Count active promises for a couple
     * 
     * @param coupleId Internal couple ID
     * @return Count of active promises
     */
    @Query("SELECT COUNT(p) FROM Promise p WHERE p.couple.id = :coupleId AND p.status = 'ACTIVE'")
    int countActiveByCoupleId(@Param("coupleId") Long coupleId);

    /**
     * Count promises pending approval from specific user
     * 
     * @param coupleId Internal couple ID
     * @param isUser1 true if checking for user1's pending approvals
     * @return Count of pending approvals
     */
    @Query("SELECT COUNT(p) FROM Promise p WHERE p.couple.id = :coupleId " +
           "AND p.status = 'PENDING' " +
           "AND (:isUser1 = true AND p.user1Approved = false " +
           "OR :isUser1 = false AND p.user2Approved = false)")
    int countPendingApprovalsForUser(@Param("coupleId") Long coupleId, 
                                     @Param("isUser1") boolean isUser1);

    /**
     * Count completed promises for a couple
     * 
     * @param couple Couple entity
     * @return Count of completed promises
     */
    @Query("SELECT COUNT(p) FROM Promise p WHERE p.couple = :couple AND p.status = 'COMPLETED'")
    long countCompletedByCouple(@Param("couple") Couple couple);

    /**
     * Count broken promises for a couple
     * 
     * @param couple Couple entity
     * @return Count of broken promises
     */
    @Query("SELECT COUNT(p) FROM Promise p WHERE p.couple = :couple AND p.status = 'BROKEN'")
    long countBrokenByCouple(@Param("couple") Couple couple);

    /**
     * Find promises created by specific user (initiator)
     * Useful for tracking who created which promise
     * 
     * @param couple Couple entity
     * @param isUser1Creator true if user1 is creator
     * @return List of promises
     */
    @Query("SELECT p FROM Promise p WHERE p.couple = :couple " +
           "AND (:isUser1Creator = true AND p.user1Approved = true AND p.user2Approved = false " +
           "OR :isUser1Creator = false AND p.user2Approved = true AND p.user1Approved = false)")
    List<Promise> findPromisesCreatedByUser(@Param("couple") Couple couple, 
                                            @Param("isUser1Creator") boolean isUser1Creator);

    /**
     * Find all promises ordered by creation date (newest first)
     * 
     * @param couple Couple entity
     * @return List of promises ordered by date
     */
    @Query("SELECT p FROM Promise p WHERE p.couple = :couple ORDER BY p.id DESC")
    List<Promise> findByCoupleOrderByCreatedDateDesc(@Param("couple") Couple couple);
}
