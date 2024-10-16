package com.org.project.service;

import com.org.project.exception.UnauthorizedException;
import com.org.project.dto.LoginRequestDTO;
import com.org.project.model.User;
import com.org.project.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByEmailAndProvider(loginRequest.getEmail(), loginRequest.getProvider())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getProvider() != User.Provider.LOCAL) {
            throw new UnauthorizedException("This account uses " + user.getProvider() + " for login");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return user;
    }
}