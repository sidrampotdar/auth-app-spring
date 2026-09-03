package com.example.spring_auth_app.spring_auth_app.controllers;

import com.example.spring_auth_app.spring_auth_app.dtos.CreateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserDto;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;
import com.example.spring_auth_app.spring_auth_app.services.implementations.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("register")
    public ResponseEntity<UserResponse> register(@RequestBody CreateUserRequest createUserRequest) {

        return new ResponseEntity<>(authService.registerUser(createUserRequest), HttpStatus.CREATED);


    }
}
