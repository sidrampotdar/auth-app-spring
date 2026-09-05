package com.example.spring_auth_app.spring_auth_app.repositories;

import com.example.spring_auth_app.spring_auth_app.models.RefreshToken;
import com.example.spring_auth_app.spring_auth_app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
}
