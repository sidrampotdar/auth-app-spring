package com.example.spring_auth_app.spring_auth_app.dtos;

import com.example.spring_auth_app.spring_auth_app.models.Provider;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String name;
    private String image;
    private Boolean enable;
    private Provider provider;
    private Set<RoleDto> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
