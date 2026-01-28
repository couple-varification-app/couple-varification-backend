package com.relationshipplatform.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "breakups")
public class Breakup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String breakupId;

    @OneToOne
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @ManyToOne
    @JoinColumn(name = "initiated_by")
    private User initiatedBy;

    private LocalDate breakupDate;

    private boolean patchupAllowed;
}
