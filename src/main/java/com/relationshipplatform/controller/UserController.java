package com.relationshipplatform.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.relationshipplatform.dto.request.user.UserVerifyRequestDto;
import com.relationshipplatform.dto.response.user.UserResponseDto;
import com.relationshipplatform.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
@RestController
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users/{userId}/verify")
    public UserResponseDto verifyUser(
            @PathVariable String userId,
            @Valid @RequestBody UserVerifyRequestDto dto) {

        return userService.verifyUser(userId, dto);
    }
}
