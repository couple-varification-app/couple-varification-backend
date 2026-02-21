package com.relationshipplatform.serviceImpl;

import com.relationshipplatform.dto.request.login.UserLoginRequest;
import com.relationshipplatform.dto.request.user.UserRegistrationRequest;
import com.relationshipplatform.dto.response.auth.AuthResponse;
import com.relationshipplatform.dto.response.relationship.RelationshipStatusInfo;
import com.relationshipplatform.entity.Couple;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.exception.*;
import com.relationshipplatform.mapper.UserMapper;
import com.relationshipplatform.repository.CoupleRepository;
import com.relationshipplatform.repository.UserRepository;
import com.relationshipplatform.security.CustomUserDetails;
import com.relationshipplatform.security.JwtService;
import com.relationshipplatform.service.AuthService;
import com.relationshipplatform.utility.AgeCalculator;
import com.relationshipplatform.utility.IdGenerator;
import com.relationshipplatform.utility.PasswordEncoderUtil;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService
 * Handles authentication, registration, and JWT token management
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final PasswordEncoderUtil passwordEncoder;
    private final JwtService jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           CoupleRepository coupleRepository,
                           UserMapper userMapper,
                           AuthenticationManager authenticationManager,
                           PasswordEncoderUtil passwordEncoder,
                           JwtService jwtUtil) {
        this.userRepository = userRepository;
        this.coupleRepository = coupleRepository;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {

        // 1. Validate age (18+)
        int age = AgeCalculator.calculateAge(request.getDob());
        if (!AgeCalculator.isEligibleAge(request.getDob())) {
            log.warn("Age verification failed for user. Age: {}", age);
            throw new AgeVerificationException(age);
        }

        // 2. Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        // 3. Create user entity
        User user = User.builder()
                .userId(IdGenerator.generateUserId())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dob(request.getDob())
                .age(age)
                .active(true)
                .verified(false)
                .build();

        // 5. Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());

        // 6. Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getEmail());

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
                .issuedAt(LocalDateTime.now())
                .relationshipStatus(relationshipStatus)
                .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                .user(userMapper.toResponse(savedUser))
                .message("Registration successful")
                .firstLogin(true)
                .build();
    }

    /** Here we have added fully spring security implementation using 
     * userDetailsService and authenticationManager
    */
   
    @Override
    public AuthResponse login(UserLoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // 1. Spring Security handles everything:
    //    - Calls CustomUserDetailsService.loadUserByUsername()
    //    - Verifies BCrypt password via DaoAuthenticationProvider
    //    - Calls CustomUserDetails.isEnabled() → throws DisabledException if inactive
    //    - Throws BadCredentialsException for wrong password
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword() 
            )
    );

    // 2. Principal is your CustomUserDetails — cast to get the real User entity
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    User user = userDetails.getUser();

    // 3. Generate JWT
    String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());

    // 4. Your custom business logic
    RelationshipStatusInfo relationshipStatus = buildRelationshipStatus(user);

    log.info("User logged in successfully: {}", user.getUserId());

    return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
            .user(userMapper.toResponse(user))
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
            User partner = activeCouple.getUser1().getId().equals(user.getId()) ? activeCouple.getUser2() : activeCouple.getUser1();

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




    

    // ==================== Below code will used to implement the refresh token ====================


    // @Override
    // public AuthResponse refreshToken(String oldToken) {
    //     log.info("Token refresh request received");

    //     try {
    //         // 1. Extract user info from old token
    //         String userId = jwtUtil.extractUserId(oldToken);
    //         String email = jwtUtil.extractUsername(oldToken);

    //         // 2. Validate old token is not expired (can refresh before expiry)
    //         if (jwtUtil.isTokenExpired(oldToken)) {
    //             throw new AuthenticationException("Token has expired. Please login again.");
    //         }

    //         // 3. Find user
    //         User user = userRepository.findByUserId(userId)
    //                 .orElseThrow(() -> new AuthenticationException("User not found"));

    //         // 4. Check if account is still active
    //         if (!user.isActive()) {
    //             throw new AuthenticationException("Account is deactivated");
    //         }

    //         // 5. Generate new token
    //         String newToken = jwtUtil.generateToken(userId, email);
            
    //         // 6. Calculate token expiration
    //         Date issuedAt = jwtUtil.extractIssuedAt(newToken);
    //         Date expiresAt = jwtUtil.extractExpiration(newToken);
    //         Long expiresIn = jwtUtil.getExpirationInSeconds();

    //         // 7. Get relationship status
    //         RelationshipStatusInfo relationshipStatus = buildRelationshipStatus(user);

    //         // 8. Build response
    //         UserResponse userResponse = userMapper.toResponse(user);

    //         log.info("Token refreshed successfully for user: {}", userId);

    //         return AuthResponse.builder()
    //                 .token(newToken)
    //                 .tokenType("Bearer")
    //                 .expiresIn(expiresIn)
    //                 .issuedAt(toLocalDateTime(issuedAt))
    //                 .expiresAt(toLocalDateTime(expiresAt))
    //                 .user(userResponse)
    //                 .message("Token refreshed successfully")
    //                 .firstLogin(false)
    //                 .build();

    //     } catch (Exception e) {
    //         log.error("Token refresh failed: {}", e.getMessage());
    //         throw new AuthenticationException("Invalid token");
    //     }
    // }
}