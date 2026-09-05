package com.example.spring_auth_app.spring_auth_app.services;

import com.example.spring_auth_app.spring_auth_app.models.RefreshToken;
import com.example.spring_auth_app.spring_auth_app.models.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String email);
    RefreshToken verifyRefreshToken(RefreshToken token);
    void deleteRefreshToken(String refreshToken);
    void deleteByUser(User user);
}
