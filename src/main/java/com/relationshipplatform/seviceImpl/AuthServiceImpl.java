package com.relationshipplatform.service.impl;

import com.relationshipplatform.dto.request.login.UserLoginRequest;
import com.relationshipplatform.dto.request.user.UserRegistrationRequest;
import com.relationshipplatform.dto.response.auth.AuthResponse;
import com.relationshipplatform.dto.response.relationship.RelationshipStatusInfo;
import com.relationshipplatform.dto.response.user.UserResponse;
import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.exception.*;
import com.relationshipplatform.mapper.UserMapper;
import com.relationshipplatform.repository.CoupleRepository;
import com.relationshipplatform.repository.UserRepository;
import com.relationshipplatform.security.JwtUtil;
import com.relationshipplatform.service.AuthService;
import com.relationshipplatform.utility.AgeCalculator;
import com.relationshipplatform.utility.IdGenerator;
import com.relationshipplatform.utility.PasswordEncoderUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Implementation of AuthService
 * Handles authentication, registration, and JWT token management
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoderUtil passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           CoupleRepository coupleRepository,
                           UserMapper userMapper,
                           PasswordEncoderUtil passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.coupleRepository = coupleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // 1. Validate age (18+)
        int age = AgeCalculator.calculateAge(request.getDob());
        if (!AgeCalculator.isEligibleAge(request.getDob())) {
            log.warn("Age verification failed for user. Age: {}", age);
            throw new AgeVerificationException(age);
        }

        // 2. Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email registration attempt: {}", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // 3. Generate unique User ID
        String userId = IdGenerator.generateUserId();
        while (userRepository.existsByUserId(userId)) {
            userId = IdGenerator.generateUserId();
        }

        // 4. Create user entity
        User user = new User();
        user.setUserId(userId);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDob(request.getDob());
        user.setAge(age);
        user.setVerified(false);
        user.setActive(true);

        // 5. Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());

        // 6. Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getEmail());
        
        // 7. Calculate token expiration
        Date issuedAt = jwtUtil.extractIssuedAt(token);
        Date expiresAt = jwtUtil.extractExpiration(token);
        Long expiresIn = jwtUtil.getExpirationInSeconds();

        // 8. Build response
        UserResponse userResponse = userMapper.toResponse(savedUser);
        
        // New user has no relationship
        RelationshipStatusInfo relationshipStatus = RelationshipStatusInfo.builder()
                .hasActiveCouple(false)
                .coupleId(null)
                .partnerName(null)
                .relationshipHealth(null)
                .hasPendingCoupleRequest(false)
                .pendingRequestFrom(null)
                .build();

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .issuedAt(toLocalDateTime(issuedAt))
                .expiresAt(toLocalDateTime(expiresAt))
                .user(userResponse)
                .relationshipStatus(relationshipStatus)
                .message("Registration successful")
                .firstLogin(true)
                .build();
    }

    @Override
    public AuthResponse login(UserLoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email: {}", request.getEmail());
                    return new AuthenticationException("Invalid email or password");
                });

        // 2. Check if account is active
        if (!user.isActive()) {
            log.warn("Login attempt for inactive account: {}", request.getEmail());
            throw new AuthenticationException("Account is deactivated. Please contact support.");
        }

        // 3. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email: {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        // 4. Generate JWT token
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());
        
        // 5. Calculate token expiration
        Date issuedAt = jwtUtil.extractIssuedAt(token);
        Date expiresAt = jwtUtil.extractExpiration(token);
        Long expiresIn = jwtUtil.getExpirationInSeconds();

        // 6. Get relationship status
        RelationshipStatusInfo relationshipStatus = buildRelationshipStatus(user);

        // 7. Build response
        UserResponse userResponse = userMapper.toResponse(user);

        log.info("User logged in successfully: {}", user.getUserId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .issuedAt(toLocalDateTime(issuedAt))
                .expiresAt(toLocalDateTime(expiresAt))
                .user(userResponse)
                .relationshipStatus(relationshipStatus)
                .message("Login successful")
                .firstLogin(false)
                .build();
    }

    @Override
    public void logout(String token) {
        log.info("Logout request received");
        // JWT is stateless, logout is handled client-side by removing token
        // If you need server-side logout, implement token blacklist in Redis
        log.info("User logged out (token invalidated client-side)");
    }

    @Override
    public AuthResponse refreshToken(String oldToken) {
        log.info("Token refresh request received");

        try {
            // 1. Extract user info from old token
            String userId = jwtUtil.extractUserId(oldToken);
            String email = jwtUtil.extractUsername(oldToken);

            // 2. Validate old token is not expired (can refresh before expiry)
            if (jwtUtil.isTokenExpired(oldToken)) {
                throw new AuthenticationException("Token has expired. Please login again.");
            }

            // 3. Find user
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            // 4. Check if account is still active
            if (!user.isActive()) {
                throw new AuthenticationException("Account is deactivated");
            }

            // 5. Generate new token
            String newToken = jwtUtil.generateToken(userId, email);
            
            // 6. Calculate token expiration
            Date issuedAt = jwtUtil.extractIssuedAt(newToken);
            Date expiresAt = jwtUtil.extractExpiration(newToken);
            Long expiresIn = jwtUtil.getExpirationInSeconds();

            // 7. Get relationship status
            com.relationshipplatform.dto.response.auth.AuthResponse.RelationshipStatusInfo relationshipStatus = buildRelationshipStatus(user);

            // 8. Build response
            UserResponse userResponse = userMapper.toResponse(user);

            log.info("Token refreshed successfully for user: {}", userId);

            return AuthResponse.builder()
                    .token(newToken)
                    .tokenType("Bearer")
                    .expiresIn(expiresIn)
                    .issuedAt(toLocalDateTime(issuedAt))
                    .expiresAt(toLocalDateTime(expiresAt))
                    .user(userResponse)
                    .relationshipStatus(relationshipStatus)
                    .message("Token refreshed successfully")
                    .firstLogin(false)
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid token");
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            return jwtUtil.validateToken(token, email);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }

    /**
     * Build relationship status information for user
     * Checks for active couple and pending requests
     */
    private RelationshipStatusInfo buildRelationshipStatus(User user) {
        // Check for active couple
        Optional<Couple> activeCoupleOpt = coupleRepository.findActiveCoupleByUser(user);
        
        if (activeCoupleOpt.isPresent()) {
            Couple activeCouple = activeCoupleOpt.get();
            User partner = activeCouple.getUser1().getId().equals(user.getId()) 
                    ? activeCouple.getUser2() 
                    : activeCouple.getUser1();

            return RelationshipStatusInfo.builder()
                    .hasActiveCouple(true)
                    .coupleId(activeCouple.getCoupleId())
                    .partnerName(partner.getName())
                    .relationshipHealth(activeCouple.getRelationshipHealth())
                    .hasPendingCoupleRequest(false)
                    .pendingRequestFrom(null)
                    .build();
        }

        // Check for pending couple request where user is partner (user2)
        Optional<Couple> pendingCoupleOpt = coupleRepository.findPendingCoupleRequestForUser(user);
        
        if (pendingCoupleOpt.isPresent()) {
            Couple pendingCouple = pendingCoupleOpt.get();
            User initiator = pendingCouple.getUser1();

            return RelationshipStatusInfo.builder()
                    .hasActiveCouple(false)
                    .coupleId(null)
                    .partnerName(null)
                    .relationshipHealth(null)
                    .hasPendingCoupleRequest(true)
                    .pendingRequestFrom(initiator.getUserId())
                    .build();
        }

        // User is single
        return RelationshipStatusInfo.builder()
                .hasActiveCouple(false)
                .coupleId(null)
                .partnerName(null)
                .relationshipHealth(null)
                .hasPendingCoupleRequest(false)
                .pendingRequestFrom(null)
                .build();
    }

    /**
     * Convert Date to LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}