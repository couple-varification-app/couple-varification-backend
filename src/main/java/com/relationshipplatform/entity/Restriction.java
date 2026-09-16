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
@Table(name = "restrictions")
public class Restriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String restrictionId;

    @ManyToOne
    @JoinColumn(name = "couple_id")
    private Couple couple;

    private String description;

    private boolean user1Approved;

    private boolean user2Approved;

    private LocalDate startDate;

    private String category;

    private String status;

    private LocalDate endDate;

    private String severity;
    
    private LocalDateTime createdAt;
}