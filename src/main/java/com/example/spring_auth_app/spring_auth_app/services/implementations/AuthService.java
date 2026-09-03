package com.example.spring_auth_app.spring_auth_app.services.implementations;

import com.example.spring_auth_app.spring_auth_app.dtos.CreateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserDto;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;

public interface AuthService {
    UserResponse registerUser(CreateUserRequest userDto);
    //UserResponse loginUser(UserDto userDto);
}
