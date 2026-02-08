package com.relationshipplatform.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;   // Public User ID

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private LocalDate dob;

    private Integer age;

    private boolean verified;

    private boolean active;

    @OneToOne(mappedBy = "user1")
    private Couple coupleAsUser1;

    @OneToOne(mappedBy = "user2")
    private Couple coupleAsUser2;

    /**
     * User roles for authorization
     * Default: ROLE_USER
     * Can have multiple roles: [ROLE_USER, ROLE_ADMIN]
     * 
     * Stored as separate table: user_roles
     * Eagerly fetched to avoid LazyInitializationException
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();


    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Set default role if no roles assigned
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(Role.ROLE_USER);
        }
    }



     @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    // ========================================
    // HELPER METHODS FOR ROLES
    
    /**
     * Add a role to this user
     */
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * Remove a role from this user
     */
    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(Role role) {
        return this.roles != null && this.roles.contains(role);
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole(Role.ROLE_ADMIN);
    }

    /**
     * Check if user is moderator
     */
    public boolean isModerator() {
        return hasRole(Role.ROLE_MODERATOR);
    }

















}
