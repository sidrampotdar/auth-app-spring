package com.example.spring_auth_app.spring_auth_app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter executed once per request to intercept HTTP requests, extract JWT from Header or Cookie,
 * validate the token, and establish authentication in Spring Security Context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        // 1. Check Authorization header ("Bearer <token>")
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            token = requestHeader.substring(7);
            try {
                username = this.jwtHelper.getUsernameFromToken(token);
            } catch (Exception e) {
                logger.error("Error extracting username from Bearer token: {}", e.getMessage());
            }
        } 
        // 2. Fallback: Check HTTP Cookies for accessToken cookie
        else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    try {
                        username = this.jwtHelper.getUsernameFromToken(token);
                    } catch (Exception e) {
                        logger.error("Error extracting username from Cookie token: {}", e.getMessage());
                    }
                    break;
                }
            }
        }

        // 3. If token contains valid username and SecurityContext has no active authentication, authenticate user
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);
            if (Boolean.TRUE.equals(validateToken)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in Spring Security Context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.info("Validation failed for JWT token");
            }
        }

        filterChain.doFilter(request, response);
    }
}
