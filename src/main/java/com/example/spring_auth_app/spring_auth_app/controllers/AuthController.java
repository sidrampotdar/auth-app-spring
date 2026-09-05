package com.example.spring_auth_app.spring_auth_app.controllers;

import com.example.spring_auth_app.spring_auth_app.dtos.AuthResponse;
import com.example.spring_auth_app.spring_auth_app.dtos.LoginRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.RefreshTokenRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;
import com.example.spring_auth_app.spring_auth_app.models.RefreshToken;
import com.example.spring_auth_app.spring_auth_app.models.User;
import com.example.spring_auth_app.spring_auth_app.repositories.RefreshTokenRepository;
import com.example.spring_auth_app.spring_auth_app.security.JwtHelper;
import com.example.spring_auth_app.spring_auth_app.services.RefreshTokenService;
import com.example.spring_auth_app.spring_auth_app.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsible for authentication operations:
 * - Login (email & password authentication + token generation + cookie setting)
 * - Refresh Token renewal
 * - Logout & Cookie revocation
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtHelper jwtHelper;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Value("${security.jwt.access-ttl-seconds:3600}")
    private long accessTtlSeconds;

    @Value("${security.jwt.refresh-ttl-seconds:86400}")
    private long refreshTtlSeconds;

    @Value("${security.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${security.jwt.cookie-same-site:lax}")
    private String cookieSameSite;

    /**
     * User Login endpoint. Authenticates user credentials, generates access token & refresh token,
     * attaches HTTP-only cookies to response header, and returns AuthResponse.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. Authenticate user credentials
        this.doAuthenticate(request.getEmail(), request.getPassword());

        // 2. Load UserDetails and UserResponse
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        UserResponse userResponse = userService.getUserByEmail(request.getEmail());

        // 3. Generate Access Token & Refresh Token
        String accessToken = jwtHelper.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        // 4. Attach HTTP-Only Cookies to response
        attachAuthCookies(response, accessToken, refreshToken.getRefreshToken());

        // 5. Build and return AuthResponse
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .user(userResponse)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Refresh Token endpoint. Generates a new access token using a valid refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        String tokenStr = (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank())
                ? request.getRefreshToken()
                : cookieRefreshToken;

        if (tokenStr == null || tokenStr.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        // 1. Fetch refresh token from database and verify expiration
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(tokenStr)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        refreshTokenService.verifyRefreshToken(refreshToken);

        User user = refreshToken.getUser();
        UserResponse userResponse = userService.getUserByEmail(user.getEmail());

        // 2. Generate new access token
        String newAccessToken = jwtHelper.generateToken(user);

        // 3. Update accessToken cookie
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(accessTtlSeconds)
                .sameSite(cookieSameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .user(userResponse)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    /**
     * User Logout endpoint. Deletes stored refresh token and clears HTTP-only cookies.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        String tokenStr = (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank())
                ? request.getRefreshToken()
                : cookieRefreshToken;

        if (tokenStr != null && !tokenStr.isBlank()) {
            refreshTokenService.deleteRefreshToken(tokenStr);
        }

        // Clear HTTP-only cookies by setting Max-Age=0
        clearAuthCookies(response);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Helper method to authenticate credentials using AuthenticationManager.
     */
    private void doAuthenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            authenticationManager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Email or Password !!");
        }
    }

    /**
     * Helper method to set accessToken and refreshToken HTTP-only cookies.
     */
    private void attachAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(accessTtlSeconds)
                .sameSite(cookieSameSite)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(refreshTtlSeconds)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    /**
     * Helper method to clear cookies upon logout.
     */
    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
