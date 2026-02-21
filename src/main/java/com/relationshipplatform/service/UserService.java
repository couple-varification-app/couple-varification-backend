package com.relationshipplatform.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.relationshipplatform.dto.request.login.PasswordChangeRequestDto;
import com.relationshipplatform.dto.request.login.UserLoginRequest;
import com.relationshipplatform.dto.request.user.UserRegistrationRequest;
import com.relationshipplatform.dto.response.auth.AuthResponse;
import com.relationshipplatform.dto.response.user.UserResponse;
@Service
public interface UserService {

    /**
     * Register a new user
     * - Validates age (18+)
     * - Checks for duplicate email
     * - Generates unique User ID
     * - Encrypts password
     * 
     * @param request User registration details
     * @return Created user response
     */
    UserResponse registerUser(UserRegistrationRequest request);

    /**
     * Authenticate user and generate JWT token
     * 
     * @param request Login credentials
     * @return Authentication response with token
     */
    AuthResponse loginUser(UserLoginRequest request);

    /**
     * Get user details by User ID
     * 
     * @param userId Public user ID
     * @return User details
     */
    UserResponse getUserById(String userId);

    /**
     * Get user details by email
     * 
     * @param email User email
     * @return User details
     */
    UserResponse getUserByEmail(String email);

    /**
     * Check if user is in an active couple
     * 
     * @param userId Public user ID
     * @return true if user is in active couple
     */
    boolean isUserInActiveCouple(String userId);

    /**
     * Update user profile
     * 
     * @param userId Public user ID
     * @param name New name
     * @return Updated user details
     */
    UserResponse updateUserProfile(String userId, String name);

    /**
     * Change user password
     * 
     * @param userId Public user ID
     * @param request Password change details
     */
    void changePassword(String userId, PasswordChangeRequestDto request);


    /**
     * Deactivate user account
     * 
     * @param userId Public user ID
     */
    void deactivateUser(String userId);

    void reactivateUser(String userId);

    void activateUser(String userId);

    public List<UserResponse> getAllUsers(Pageable pageable, String search);
    
}
