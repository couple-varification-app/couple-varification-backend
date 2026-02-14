package com.relationshipplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * FIXED JWT Authentication Filter
 * Properly extracts and validates JWT tokens from Authorization header
 * 
 * Key Fix: Properly handles "Bearer " prefix and trims whitespace
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain filterChain) throws ServletException, IOException {

        // 1. Get Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request headers");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // ========================================
            // 🔧 FIX: PROPERLY EXTRACT TOKEN
            // ========================================
            // Extract token after "Bearer " and TRIM whitespace
            final String jwt = authHeader.substring(7).trim();  // 🆕 Added .trim()
            
            log.debug("Extracted JWT token: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");

            // 3. Extract username from token
            final String userEmail = jwtUtil.extractUsername(jwt);

            log.debug("Extracted email from token: {}", userEmail);

            // 4. Check if user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 5. Load user details
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                log.debug("Loaded user details for: {}", userEmail);
                log.debug("User authorities: {}", userDetails.getAuthorities());

                // 6. Validate token
                if (jwtUtil.isValidateToken(jwt, userDetails)) {  // Here i passed userDetails instead of String username only
                    
                    // 7. Create authentication token
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()  // 🔑 Include authorities (roles)
                        );

                    // 8. Set authentication details
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 9. Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("User {} authenticated successfully with roles: {}", 
                             userEmail, userDetails.getAuthorities());
                } else {
                    log.warn("JWT token validation failed for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Don't throw exception - let request continue to be rejected by security
        }

        // 10. Continue filter chain
        filterChain.doFilter(request, response);
    }
}