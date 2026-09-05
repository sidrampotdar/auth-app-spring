package com.example.spring_auth_app.spring_auth_app.services;

import com.example.spring_auth_app.spring_auth_app.dtos.AuthResponse;
import com.example.spring_auth_app.spring_auth_app.dtos.CreateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.LoginRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    UserResponse register(CreateUserRequest request);
    AuthResponse login(LoginRequest request, HttpServletResponse response);
    AuthResponse refreshToken(String tokenStr, HttpServletResponse response);
    void logout(String tokenStr, HttpServletResponse response);
}
