package com.relationshipplatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.User;

public interface CoupleRepository extends JpaRepository<Couple, Long>{
/**
     * Find couple by public Couple ID
     */
    Optional<Couple> findByCoupleId(String coupleId);

    /**
     * Find active couple by either user
     */
    @Query("SELECT c FROM Couple c WHERE (c.user1 = :user OR c.user2 = :user) " +
           "AND c.status = 'ACTIVE'")
    Optional<Couple> findActiveCoupleByUser(@Param("user") User user);

    /**
     * Find pending couple request where user is partner
     */
    @Query("SELECT c FROM Couple c WHERE c.user2 = :user AND c.status = 'PENDING'")
    Optional<Couple> findPendingCoupleRequestForUser(@Param("user") User user);

    /**
     * Find all couples (active or pending) involving a user
     */
    @Query("SELECT c FROM Couple c WHERE (c.user1 = :user OR c.user2 = :user)")
    Optional<Couple> findCoupleByUser(@Param("user") User user);

    /**
     * Check if couple exists between two users
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Couple c WHERE ((c.user1 = :user1 AND c.user2 = :user2) " +
           "OR (c.user1 = :user2 AND c.user2 = :user1)) AND c.status != 'BROKEN'")
    boolean existsCoupleByUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Check if coupleId exists
     */
    boolean existsByCoupleId(String coupleId);

}
