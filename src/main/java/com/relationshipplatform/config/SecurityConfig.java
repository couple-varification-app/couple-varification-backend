package com.relationshipplatform.config;

import com.relationshipplatform.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Updated Security Configuration with Role-Based Authorization
 * 
 * Changes:
 * 1. Added @EnableMethodSecurity for @PreAuthorize support
 * 2. Configured endpoint authorization by role
 * 3. Public endpoints don't require authentication
 * 4. User endpoints require ROLE_USER
 * 5. Admin endpoints require ROLE_ADMIN
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 🆕 Enables @PreAuthorize, @Secured, etc.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Security filter chain configuration
     * Defines which endpoints require which roles
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for JWT)
            .csrf(csrf -> csrf.disable())
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // ========================================
                // PUBLIC ENDPOINTS (No authentication)
                // ========================================
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/verification/**",  // Public verification
                    "/error",
                    "/actuator/health",      // Health check
                    "/swagger-ui/**",        // Swagger UI
                    "/v3/api-docs/**"        // OpenAPI docs
                ).permitAll()
                
                // ========================================
                // ADMIN ENDPOINTS (Require ROLE_ADMIN)
                // ========================================
                .requestMatchers(
                    "/api/users/*/deactivate",     // Only admin can deactivate users
                    "/api/couples/all",            // View all couples
                    "/api/admin/**"                // All admin endpoints
                ).hasRole("ADMIN")  // 🆕 Requires ROLE_ADMIN
                
                // ========================================
                // USER ENDPOINTS (Require ROLE_USER or higher)
                // ========================================
                .requestMatchers(
                    "/api/users/**",
                    "/api/couples/**",
                    "/api/promises/**",
                    "/api/dreams/**",
                    "/api/restrictions/**",
                    "/api/breakups/**",
                    "/api/conflicts/**",
                    "/api/kyc/**"
                ).hasAnyRole("USER", "ADMIN", "MODERATOR")  // 🆕 Requires any role
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Stateless session (JWT-based, no session)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Add JWT filter before username/password authentication
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Authentication provider   : No need to Write explicitly spring security implement it automatically
     */
    // @Bean
    // public AuthenticationProvider authenticationProvider() {
    //     DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    //     authProvider.setUserDetailsService(userDetailsService);
    //     authProvider.setPasswordEncoder(passwordEncoder());
    //     return authProvider;
    // }

}