package com.example.spring_auth_app.spring_auth_app.services;

import com.example.spring_auth_app.spring_auth_app.dtos.AuthResponse;
import com.example.spring_auth_app.spring_auth_app.dtos.CreateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.LoginRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;
import com.example.spring_auth_app.spring_auth_app.models.RefreshToken;
import com.example.spring_auth_app.spring_auth_app.models.User;
import com.example.spring_auth_app.spring_auth_app.repositories.RefreshTokenRepository;
import com.example.spring_auth_app.spring_auth_app.security.JwtHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Service implementation for handling authentication operations:
 * - Public User Registration
 * - Login (credential verification + token generation + HTTP-only cookies)
 * - Refresh Token renewal
 * - Logout & cookie revocation
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtHelper jwtHelper;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.access-ttl-seconds:3600}")
    private long accessTtlSeconds;

    @Value("${security.jwt.refresh-ttl-seconds:86400}")
    private long refreshTtlSeconds;

    @Value("${security.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${security.jwt.cookie-same-site:lax}")
    private String cookieSameSite;

    @Override
    public UserResponse register(CreateUserRequest request) {
        // Public registration delegates to user creation logic (creates user, hashes password, assigns default ROLE_USER)
        return userService.createUser(request);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        // 1. Authenticate credentials
        doAuthenticate(request.getEmail(), request.getPassword());

        // 2. Load UserDetails and UserResponse
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        UserResponse userResponse = userService.getUserByEmail(request.getEmail());

        // 3. Generate Access Token & Refresh Token
        String accessToken = jwtHelper.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        // 4. Attach HTTP-Only Cookies
        attachAuthCookies(response, accessToken, refreshToken.getRefreshToken());

        // 5. Build AuthResponse
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .user(userResponse)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String tokenStr, HttpServletResponse response) {
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

        // 3. Update accessToken HTTP-only cookie
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(accessTtlSeconds)
                .sameSite(cookieSameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .user(userResponse)
                .build();
    }

    @Override
    public void logout(String tokenStr, HttpServletResponse response) {
        if (tokenStr != null && !tokenStr.isBlank()) {
            refreshTokenService.deleteRefreshToken(tokenStr);
        }
        // Clear HTTP-only cookies by setting Max-Age=0
        clearAuthCookies(response);
    }

    private void doAuthenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Email or Password !!");
        }
    }

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
