package com.relationshipplatform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.relationshipplatform.entity.User;

public interface UserRepository extends JpaRepository <User, Long>{
 /**
     * Find user by public User ID
     */
    Optional<User> findByUserId(String userId);

    Page <User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if userId already exists
     */
    boolean existsByUserId(String userId);

    /**
     * Find active user by email
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * Check if user is part of any active couple
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Couple c WHERE (c.user1 = :user OR c.user2 = :user) " +
           "AND c.status = 'ACTIVE'")
    boolean isUserInActiveCouple(@Param("user") User user);

}
