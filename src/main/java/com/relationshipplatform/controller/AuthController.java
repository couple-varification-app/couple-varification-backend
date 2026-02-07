package com.relationshipplatform.controller;

import com.relationshipplatform.dto.request.login.UserLoginRequest;
import com.relationshipplatform.dto.request.user.UserRegistrationRequest;
import com.relationshipplatform.dto.response.api.ApiResponse;
import com.relationshipplatform.dto.response.auth.AuthResponse;
import com.relationshipplatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication operations
 * Base URL: /api/auth
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user
     * POST /api/auth/register
     * 
     * Request Body:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "password": "SecurePass@123",
     *   "dob": "1995-05-15"
     * }
     * 
     * Response: AuthResponse with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        
        log.info("POST /api/auth/register - Registering user: {}", request.getEmail());
        
        AuthResponse authResponse = authService.register(request);
        ApiResponse<AuthResponse> response = ApiResponse.success(
            authResponse, 
            "User registered successfully"
        );
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Login user
     * POST /api/auth/login
     * 
     * Request Body:
     * {
     *   "email": "john@example.com",
     *   "password": "SecurePass@123"
     * }
     * 
     * Response: AuthResponse with JWT token and relationship status
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody UserLoginRequest request) {
        
        log.info("POST /api/auth/login - Login attempt: {}", request.getEmail());
        
        AuthResponse authResponse = authService.login(request);
        ApiResponse<AuthResponse> response = ApiResponse.success(
            authResponse, 
            "Login successful"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user
     * POST /api/auth/logout
     * Header: Authorization: Bearer {token}
     * 
     * Note: JWT is stateless, logout is primarily client-side
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("POST /api/auth/logout - Logout request");
        
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        
        ApiResponse<Void> response = ApiResponse.success("Logged out successfully");
        return ResponseEntity.ok(response);
    }

    // /**
    //  * Refresh JWT token
    //  * POST /api/auth/refresh
    //  * Header: Authorization: Bearer {token}
    //  * 
    //  * Response: New AuthResponse with refreshed token
    //  */
    // @PostMapping("/refresh")
    // public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
    //         @RequestHeader("Authorization") String authHeader) {
        
    //     log.info("POST /api/auth/refresh - Token refresh request");
        
    //     String oldToken = authHeader.replace("Bearer ", "");
    //     AuthResponse authResponse = authService.refreshToken(oldToken);
        
    //     ApiResponse<AuthResponse> response = ApiResponse.success(
    //         authResponse, 
    //         "Token refreshed successfully"
    //     );
        
    //     return ResponseEntity.ok(response);
    // }

    
    /**
     * Validate JWT token
     * GET /api/auth/validate
     * Header: Authorization: Bearer {token}
     * 
     * Response: Boolean indicating token validity
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("GET /api/auth/validate - Token validation request");
        
        String token = authHeader.replace("Bearer ", "");
        boolean isValid = authService.validateToken(token);
        
        ApiResponse<Boolean> response = ApiResponse.success(
            isValid, 
            isValid ? "Token is valid" : "Token is invalid"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user from token
     * GET /api/auth/me
     * Header: Authorization: Bearer {token}
     * 
     * Response: User information
     */
    
}