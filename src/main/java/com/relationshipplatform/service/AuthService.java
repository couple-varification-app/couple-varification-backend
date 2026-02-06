package com.relationshipplatform.service;

import com.relationshipplatform.dto.request.login.UserLoginRequest;
import com.relationshipplatform.dto.request.user.UserRegistrationRequest;
import com.relationshipplatform.dto.response.auth.AuthResponse;

/**
 * Service interface for Authentication operations
 */
public interface AuthService {

    /**
     * Register a new user and return auth token
     * - Validates age (18+)
     * - Checks for duplicate email
     * - Generates unique User ID
     * - Encrypts password
     * - Generates JWT token
     * 
     * @param request User registration details
     * @return AuthResponse with token and user info
     */
    AuthResponse register(UserRegistrationRequest request);

    /**
     * Authenticate user and generate JWT token
     * - Validates credentials
     * - Checks account status
     * - Generates JWT token
     * - Returns relationship status
     * 
     * @param request Login credentials
     * @return AuthResponse with token and relationship status
     */
    AuthResponse login(UserLoginRequest request);

    /**
     * Logout user (invalidate token)
     * Note: JWT is stateless, so this is primarily for client-side
     * Can be extended with token blacklist if needed
     * 
     * @param token JWT token to invalidate
     */
    void logout(String token);

    /**
     * Refresh JWT token
     * Generate new token before expiration
     * 
     * @param oldToken Current JWT token
     * @return New AuthResponse with refreshed token
     */
    AuthResponse refreshToken(String oldToken);

    /**
     * Validate JWT token
     * 
     * @param token JWT token
     * @return true if token is valid
     */
    boolean validateToken(String token);

    /**
     * Extract user ID from JWT token
     * 
     * @param token JWT token
     * @return User ID
     */
    String getUserIdFromToken(String token);
}