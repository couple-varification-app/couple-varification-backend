package com.relationshipplatform.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.relationshipplatform.dto.response.api.ApiResponse;
import com.relationshipplatform.dto.response.user.UserResponse;
import com.relationshipplatform.service.AuthService;
import com.relationshipplatform.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;


    /**
     * Get user by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId){
        log.info("GET /api/users/{} - Fetching user",userId);

        UserResponse user = userService.getUserById(userId);
        ApiResponse<UserResponse> response = ApiResponse.success(user,"User retrieved successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by email
     * GET /api/users/email/{email}
     * 
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email){
        
        log.info("GET /api/users/email/{} Fetching user", email);

        UserResponse user = userService.getUserByEmail(email);
        ApiResponse<UserResponse> response = ApiResponse.success(user,"User retrieved successfully");
        
        return ResponseEntity.ok(response);

    }

    // @PutMapping("/{userId}/profile")
    // public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(@PathVariable String userId, @RequestParam(required = false) String name){
        
    // }




    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("GET /api/auth/me - Get current user");
        
        String token = authHeader.replace("Bearer ", "");
        String userId = authService.getUserIdFromToken(token);
        
        ApiResponse<String> response = ApiResponse.success(
            userId, 
            "User ID retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
}
