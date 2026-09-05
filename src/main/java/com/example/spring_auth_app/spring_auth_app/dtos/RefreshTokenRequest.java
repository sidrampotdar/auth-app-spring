package com.example.spring_auth_app.spring_auth_app.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Payload for requesting a new access token using a refresh token.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
