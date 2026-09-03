package com.example.spring_auth_app.spring_auth_app.services;

import com.example.spring_auth_app.spring_auth_app.dtos.CreateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;
import com.example.spring_auth_app.spring_auth_app.services.implementations.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {
    private final UserServiceImplementation userService;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserResponse registerUser(CreateUserRequest createUserRequest) {
        createUserRequest.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        return  userService.createUser(createUserRequest);

    }


}
