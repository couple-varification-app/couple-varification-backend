package com.relationshipplatform.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "couples")
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String coupleId;

    @OneToOne
    @JoinColumn(name = "user1_id")
    private User user1;

    @OneToOne
    @JoinColumn(name = "user2_id")
    private User user2;

    private String couplePin;  // encrypted

    private String status; // ACTIVE, BROKEN

    private Integer relationshipHealth;

    private Integer loyaltyScore;

    @OneToMany(mappedBy = "couple", cascade = CascadeType.ALL)
    private List<Promise> promises;

    @OneToMany(mappedBy = "couple", cascade = CascadeType.ALL)
    private List<Dream> dreams;

    @OneToMany(mappedBy = "couple", cascade = CascadeType.ALL)
    private List<Restriction> restrictions;

    @OneToOne(mappedBy = "couple", cascade = CascadeType.ALL)
    private RelationshipHealth relationshipHealthDetails;
}
