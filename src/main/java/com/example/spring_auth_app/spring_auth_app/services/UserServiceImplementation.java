package com.example.spring_auth_app.spring_auth_app.services;

import com.example.spring_auth_app.spring_auth_app.dtos.CreateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UpdateUserRequest;
import com.example.spring_auth_app.spring_auth_app.dtos.UserResponse;
import com.example.spring_auth_app.spring_auth_app.exceptions.ResourceNotFoundException;
import com.example.spring_auth_app.spring_auth_app.models.Provider;
import com.example.spring_auth_app.spring_auth_app.models.Role;
import com.example.spring_auth_app.spring_auth_app.models.User;
import com.example.spring_auth_app.spring_auth_app.repositories.RoleRepository;
import com.example.spring_auth_app.spring_auth_app.repositories.UserRepository;
import com.example.spring_auth_app.spring_auth_app.utilities.UserHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        // Hash password before saving to PostgreSQL database
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(Provider.LOCAL);

        // Assign default ROLE_USER role to newly registered user
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
        user.getRoles().add(defaultRole);

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist with this email"));
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public UserResponse updateUser(UpdateUserRequest request, String id) {
        UUID uuid = UserHelper.parseUUID(id);
        User existingUser = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist with this id"));

        if (request.getName() != null) {
            existingUser.setName(request.getName());
        }
        if (request.getImage() != null) {
            existingUser.setImage(request.getImage());
        }

        return modelMapper.map(existingUser, UserResponse.class);
    }

    @Override
    public void deleteUser(String id) {
        UUID uuid = UserHelper.parseUUID(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found by given id"));
        userRepository.delete(user);
    }

    @Override
    public UserResponse getUserById(String id) {
        UUID uuid = UserHelper.parseUUID(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found by given id"));
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public Iterable<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .toList();
    }
}
