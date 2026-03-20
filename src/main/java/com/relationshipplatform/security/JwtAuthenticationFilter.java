package com.relationshipplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
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

//        if (request.getServletPath().contains("swagger") ||
//                request.getServletPath().contains("v3/api-docs")) {
//            filterChain.doFilter(request, response);
//            return;
//        }

        try {

            final String jwt = authHeader.substring(7).trim();  // 🆕 Added .trim()
            
            log.debug("Extracted JWT token: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");

            // 3. Extract username from token
            final String userEmail = jwtService.extractUsername(jwt);

            log.debug("Extracted email from token: {}", userEmail);

            // 4. Check if user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // loadUserByUsername returns CustomUserDetails wrapping your real User
                CustomUserDetails userDetails =
                        (CustomUserDetails) userDetailsService.loadUserByUsername(userEmail);
                        
                log.debug("Loaded user details for: {}", userEmail);
                log.debug("User authorities: {}", userDetails.getAuthorities());

                // 6. Validate token
                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    
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
            // Log but don't throw — Spring Security will reject the request if unauthenticated
            log.error("Cannot set user authentication: {}", e.getMessage());
            
        }

        // 10. Continue filter chain
        filterChain.doFilter(request, response);
    }
}