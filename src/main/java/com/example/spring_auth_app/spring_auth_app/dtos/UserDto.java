package com.example.spring_auth_app.spring_auth_app.dtos;

import com.example.spring_auth_app.spring_auth_app.models.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private UUID id;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    private Boolean enable = true;
    private String image;
    private Provider provider;

    // ⬇️ Renamed from role to roles to match Entity definition
    private Set<RoleDto> roles;

    private Instant createdAt;
    private Instant updatedAt;
}
