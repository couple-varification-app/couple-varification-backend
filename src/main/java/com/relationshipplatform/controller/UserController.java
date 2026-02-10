package com.relationshipplatform.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.relationshipplatform.dto.request.login.PasswordChangeRequestDto;
import com.relationshipplatform.dto.response.api.ApiResponse;
import com.relationshipplatform.dto.response.user.UserResponse;
import com.relationshipplatform.service.AuthService;
import com.relationshipplatform.service.UserService;

import jakarta.validation.Valid;
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
     * Get all user details (ONLY ADMIN CAN ACCESS)
     * GET /api/users/
     */
    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(){
        log.info("GET /api/users/{} - Fetching user");

        List<UserResponse> users = userService.getAllUsers();
        ApiResponse<List<UserResponse>> response = ApiResponse.success(users,"User retrieved successfully");
        
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
    @PutMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(@PathVariable String userId, @RequestParam(required = false) String name){

        log.info("PUT /api/users/{}/profile - Updating profile",userId);        

        UserResponse user = userService.updateUserProfile(userId, name);
        ApiResponse<UserResponse> response = ApiResponse.success(user, "Profile updated successfully");

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{userId}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable String userId, @Valid @RequestBody PasswordChangeRequestDto request){

        log.info("POST /api/users/{}/change-password - Changing password",userId);

        userService.changePassword(userId, request);
        ApiResponse<Void> response = ApiResponse.success("Password changed successfully");

        return ResponseEntity.ok(response);
    }

    // ====== DINESH BHAI, WE SHOULD WORK HARD TO GET SUCCEED =======

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{userId}/couple-status")
    public ResponseEntity<ApiResponse<Boolean>> checkCoupleStatus(@PathVariable String userId){
        
        log.info("GET /api/users/{}/couple-status", userId);

        boolean isInCouple = userService.isUserInActiveCouple(userId);
        ApiResponse<Boolean> response = ApiResponse.success(isInCouple, isInCouple ? "User is in active couple" : "User is single");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable String userId){
        log.info("DELETE /api/users/{} - Deactivating User",userId);

        userService.deactivateUser(userId);
        ApiResponse<Void> response = ApiResponse.success("User Deactivated Successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/reactivate")
    public ResponseEntity<ApiResponse<Void>> reactivateUser(@PathVariable String userId){

        log.info("POST /api/{}/reactivate - Reactivating user",userId);

        userService.reactivateUser(userId);
        ApiResponse<Void> response = ApiResponse.success("User reactivated successfully");

        return ResponseEntity.ok(response);
    }


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
