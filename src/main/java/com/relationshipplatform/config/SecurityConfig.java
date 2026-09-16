package com.relationshipplatform.config;

import com.relationshipplatform.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

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

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
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
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                
                // ========================================
                // 🔓 PUBLIC ENDPOINTS
                // ========================================
                .requestMatchers(
                    // Auth endpoints
                    "/api/auth/**",
                    
                    // ✅ SWAGGER - ALL PATTERNS (This was missing!)
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/swagger-resources/**",
                    "/webjars/**",
                    
                    // Health check
                    "/actuator/health",
                    "/actuator/health/db",
                    "/error"
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
                .requestMatchers("/api/auth/me").authenticated()
                .anyRequest().authenticated()
            )
            
            // Stateless session (JWT-based, no session)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
//
//                // Add these to your securityFilterChain(HttpSecurity http)
//                .exceptionHandling(exceptions -> exceptions
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                            response.setContentType("application/json");
//                            response.getWriter().write(
//                                    "{\"success\":false,\"message\":\"Unauthorized: Token is missing or invalid\",\"errorCode\":\"UNAUTHORIZED\"}"
//                            );
//                        })
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                            response.setContentType("application/json");
//                            response.getWriter().write(
//                                    "{\"success\":false,\"message\":\"Forbidden: You do not have the required role\",\"errorCode\":\"FORBIDDEN\"}"
//                            );
//                        })
//                )
            
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
     * Authentication provider   : No need to Write explicitly spring security implemented it automatically
     */
//     @Bean
//     public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
//         DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
//         authProvider.setPasswordEncoder(passwordEncoder());
//         return authProvider;
//     }

}