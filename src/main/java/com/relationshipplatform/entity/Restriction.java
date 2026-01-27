package com.relationshipplatform.entity;

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
}