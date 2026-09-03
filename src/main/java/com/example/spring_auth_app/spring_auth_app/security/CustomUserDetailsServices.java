package com.example.spring_auth_app.spring_auth_app.security;

import com.example.spring_auth_app.spring_auth_app.exceptions.ResourceNotFoundException;
import com.example.spring_auth_app.spring_auth_app.models.User;
import com.example.spring_auth_app.spring_auth_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServices implements UserDetailsService {
    private  final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return userRepository.findByEmailWithRoles(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        ));
    }
}
