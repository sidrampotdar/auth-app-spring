package com.example.spring_auth_app.spring_auth_app.services;

import com.example.spring_auth_app.spring_auth_app.dtos.CreateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UpdateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserByEmail(String email);
    UserResponse updateUser(UpdateUserRequest request, String id);
    void deleteUser(String id);
    UserResponse getUserById(String id);
    Iterable<UserResponse> getAllUsers();
}
