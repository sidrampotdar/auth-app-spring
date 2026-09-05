package com.example.spring_auth_app.spring_auth_app.services;

import com.example.spring_auth_app.spring_auth_app.exceptions.ResourceNotFoundException;
import com.example.spring_auth_app.spring_auth_app.models.RefreshToken;
import com.example.spring_auth_app.spring_auth_app.models.User;
import com.example.spring_auth_app.spring_auth_app.repositories.RefreshTokenRepository;
import com.example.spring_auth_app.spring_auth_app.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service implementation for managing database-backed Refresh Tokens.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${security.jwt.refresh-ttl-seconds:86400}")
    private long refreshTtlSeconds;

    @Override
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Check if user already has an existing refresh token in DB
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshToken.builder()
                        .user(user)
                        .build());

        // Update token string and expiry timestamp
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken.setExpiry(Instant.now().plusSeconds(refreshTtlSeconds));

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(RefreshToken token) {
        if (token.getExpiry().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token was expired. Please log in again.");
        }
        return token;
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public void deleteByUser(User user) {
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);
    }
}
