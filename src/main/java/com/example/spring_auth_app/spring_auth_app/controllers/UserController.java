package com.example.spring_auth_app.spring_auth_app.controllers;

import com.example.spring_auth_app.spring_auth_app.dtos.CreateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UpdateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;
import com.example.spring_auth_app.spring_auth_app.services.implementations.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    public ResponseEntity<Iterable<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserRequest request, @PathVariable("id") String id) {
        return ResponseEntity.ok(userService.updateUser(request, id));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
