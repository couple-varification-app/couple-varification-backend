package com.relationshipplatform.serviceImpl;

import com.relationshipplatform.dto.request.login.PasswordChangeRequestDto;
import com.relationshipplatform.dto.request.login.UserLoginRequest;
import com.relationshipplatform.dto.request.user.UserRegistrationRequest;
import com.relationshipplatform.dto.response.auth.AuthResponse;
import com.relationshipplatform.dto.response.user.UserResponse;
import com.relationshipplatform.entity.Role;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.exception.*;
import com.relationshipplatform.mapper.UserMapper;
import com.relationshipplatform.repository.UserRepository;
import com.relationshipplatform.security.JwtService;
import com.relationshipplatform.service.UserService;
import com.relationshipplatform.utility.AgeCalculator;
import com.relationshipplatform.utility.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Implementation of UserService
 * Handles user registration, authentication, and profile management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtUtil;
    private final PasswordEncoderUtil passwordEncoder;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository, 
                               UserMapper userMapper,
                               PasswordEncoderUtil passwordEncoder,
                                JwtService jwtUtil) {
            this.userRepository = userRepository;
            this.userMapper = userMapper;
            this.jwtUtil = jwtUtil;
            this.passwordEncoder = passwordEncoder;
        }
    // Add JWT token service/utility when implementing authentication
    // private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // 1. Validate age (18+)
        int age = AgeCalculator.calculateAge(request.getDob());
        if (age < 18) {
            log.warn("Age verification failed for user. Age: {}", age);
            throw new AgeVerificationException(age);
        }

        // 2. Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email registration attempt: {}", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // 3. Generate unique User ID using UUID
        String userId = generateUniqueUserId();

        // 4. Create user entity
        User user = User.builder()
                .userId(userId)
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dob(request.getDob())
                .age(age)
                .roles(Set.of(Role.ROLE_USER))
                .verified(false)
                .active(true)
                .build();
                // user.addRole(Role.ROLE_USER);    this is another way to store role 

        // 5. Save to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());

        // 6. Return response (without sensitive data)
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse loginUser(UserLoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", request.getEmail());
                    return new AuthenticationException("Invalid email or password");
                });

        // 2. Check if account is active
        if (!user.isActive()) {
            log.warn("Inactive account login attempt: {}", request.getEmail());
            throw new AuthenticationException("Account is deactivated. Please contact support.");
        }

        // 3. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for email: {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        // 4. Generate JWT token - Implement with Spring Security/JWT
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());
        log.info("User logged in successfully: {}", user.getUserId());

        // 5. Return auth response
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .message("Login successful")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        log.info("Fetching user by ID: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserInActiveCouple(String userId) {
        log.info("Checking if user {} is in active couple", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        return userRepository.isUserInActiveCouple(user);
    }

    @Override
    @Transactional
    public void deactivateUser(String userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        // Check if user is already deactivated
        if (!user.isActive()) {
            log.warn("User {} is already deactivated", userId);
            throw new InvalidOperationException("User is already deactivated");
        }

        // Check if user is in active couple
        if (userRepository.isUserInActiveCouple(user)) {
            throw new InvalidOperationException(
                "Cannot deactivate account while in an active relationship. " +
                "Please complete breakup process first."
            );
        }

        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully: {}", userId);
    }

    @Override
    @Transactional
    public void activateUser(String userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        if (user.isActive()) {
            log.warn("User {} is already active", userId);
            throw new InvalidOperationException("User is already active");
        }

        user.setActive(true);
        userRepository.save(user);

        log.info("User activated successfully: {}", userId);
    }

    // Helper methods
    private String generateUniqueUserId() {
        String userId;
        int maxAttempts = 10;
        int attempts = 0;
        
        do {
            userId = UUID.randomUUID().toString();
            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate unique user ID after " + maxAttempts + " attempts");
            }
        } while (userRepository.existsByUserId(userId));
        
        return userId;
    }

    @Override
    @Transactional
    public void reactivateUser(String userId) {
        log.info("Reactivating user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        user.setActive(true);
        userRepository.save(user);

        log.info("User reactivated successfully: {}", userId);
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(String userId, String name) {
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        // Update name
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }

        // Recalculate age (in case DOB was updated)
        if (user.getDob() != null) {
            user.setAge(AgeCalculator.calculateAge(user.getDob()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully: {}", userId);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(String userId, PasswordChangeRequestDto request) {
        log.info("Changing password for user: {}", userId);

        // 1. Validate new password matches confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("New password and confirm password do not match");
        }

        // 2. Find user
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        // 3. Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Invalid current password attempt for user: {}", userId);
            throw new AuthenticationException("Current password is incorrect");
        }

        // 4. Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    public List<UserResponse> getAllUsers(Pageable pageable, String search) {

        List<User> users;
        if (search == null || search.isBlank()) {
                users = userRepository
                    .findAll(pageable)
                    .getContent();
        } else {
            users = userRepository      
                        .findByNameContainingIgnoreCase(search, pageable)
                        .getContent();
        }

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }
}