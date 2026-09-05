package com.example.spring_auth_app.spring_auth_app.dtos;

import lombok.*;

/**
 * Authentication response sent back after successful login or token refresh.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
