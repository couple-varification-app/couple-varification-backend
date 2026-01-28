package com.relationshipplatform.dto.response.user;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String userId;
    private String name;
    private String email;
    private Integer age;
    private boolean verified;
    private boolean active;
}
