package com.relationshipplatform.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
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
}
