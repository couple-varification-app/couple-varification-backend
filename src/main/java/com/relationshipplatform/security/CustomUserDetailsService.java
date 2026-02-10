package com.relationshipplatform.security;

import com.relationshipplatform.entity.Role;
import com.relationshipplatform.entity.User;
import com.relationshipplatform.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
        // Convert roles to GrantedAuthorities
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user.getRoles());

        log.debug("User loaded successfully: {} with roles: {}", email, authorities);

        // Return Spring Security UserDetails
        // We're using email as username, password is already encrypted
        // No roles/authorities for now (can be added later)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities) // Empty authorities for now
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }


    // ============= IMPORTANT FOR LATER ======================
//     6. Proper long-term design (production-ready)

// Later, when you add roles to DB:

/**
     * Convert Role enum to Spring Security GrantedAuthority
     * 
     * @param roles Set of Role enums
     * @return Collection of GrantedAuthority
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            // Default to ROLE_USER if no roles assigned
            return Set.of(new SimpleGrantedAuthority(Role.ROLE_USER.name()));
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

}