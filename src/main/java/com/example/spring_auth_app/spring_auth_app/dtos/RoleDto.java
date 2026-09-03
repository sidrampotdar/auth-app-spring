package com.example.spring_auth_app.spring_auth_app.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {

    private UUID id;
    private String name;
}
