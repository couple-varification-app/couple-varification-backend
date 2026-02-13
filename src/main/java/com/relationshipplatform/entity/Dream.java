package com.relationshipplatform.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "dreams")
public class Dream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dreamId;

    @ManyToOne
    @JoinColumn(name = "couple_id")
    private Couple couple;

    private String description;

     private String status; // ACTIVE, ACHIEVED, ABANDONED
    private String category; //Values: TRAVEL, FINANCIAL, FAMILY, CAREER, HEALTH, HOME, etc.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate targetDate;
    private LocalDateTime achievedAt; // When dream was achieved
    
    // Optional: Include basic couple info
    private String user1Name;
    private String user2Name;
    
    // Computed fields
    private boolean isAchieved;
    private Long daysActive; // How long dream has been active
}
