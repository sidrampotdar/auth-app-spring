package com.example.spring_auth_app.spring_auth_app.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id")
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;
}