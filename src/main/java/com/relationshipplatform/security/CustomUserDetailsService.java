package com.relationshipplatform.security;

import com.relationshipplatform.entity.User;
import com.relationshipplatform.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Custom UserDetailsService implementation
 * Loads user data for Spring Security authentication
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Check if user is active
        if (!user.isActive()) {
            log.warn("Inactive user attempted to authenticate: {}", email);
            throw new UsernameNotFoundException("User account is deactivated");
        }

        log.debug("User loaded successfully: {}", email);

        // Return Spring Security UserDetails
        // We're using email as username, password is already encrypted
        // No roles/authorities for now (can be added later)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER") // Empty authorities for now
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }


    // ============= IMPORTANT FOR LATER ======================
//     6. Proper long-term design (production-ready)

// Later, when you add roles to DB:

// Example DB roles
// USER
// ADMIN

// Map them correctly
// List<GrantedAuthority> authorities =
//         user.getRoles().stream()
//             .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//             .toList();


// And pass that list into .authorities(...).

}