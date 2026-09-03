package com.example.spring_auth_app.spring_auth_app.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {

    private String name;
    private String image;
}
