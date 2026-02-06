package com.relationshipplatform.seviceImpl;

import com.relationshipplatform.dto.request.login.UserLoginRequest;
import com.relationshipplatform.dto.request.user.UserRegistrationRequest;
import com.relationshipplatform.dto.response.auth.AuthResponse;
import com.relationshipplatform.dto.response.user.UserResponse;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.exception.*;
import com.relationshipplatform.mapper.UserMapper;
import com.relationshipplatform.repository.UserRepository;
import com.relationshipplatform.service.UserService;
import com.relationshipplatform.utility.AgeCalculator;
import com.relationshipplatform.utility.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoderUtil passwordEncoder;
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
                .verified(false)
                .active(true)
                .build();

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
        String token = generateJwtToken(user); // Replace with actual JWT generation

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

    private String generateJwtToken(User user) {
        // TODO: Implement JWT token generation
        // return jwtTokenProvider.generateToken(user.getEmail(), user.getUserId(), user.getRoles());
        
        // Temporary placeholder - replace with actual JWT implementation
        return "JWT_TOKEN_" + user.getUserId() + "_" + System.currentTimeMillis();
    }
}