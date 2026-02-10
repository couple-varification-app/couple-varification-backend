package com.relationshipplatform.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for user data in responses
 * Does NOT include sensitive information like password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String userId;
    private String name;
    private String email;
    private LocalDate dob;
    private Integer age;
    private boolean verified;
    private boolean active;
    private String currentCoupleId; // null if single
}