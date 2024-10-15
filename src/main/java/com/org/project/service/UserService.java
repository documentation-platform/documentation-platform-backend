package com.org.project.service;

import com.org.project.model.User;
import com.org.project.model.auth.RegisterRequestDTO;
import com.org.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public User registerUser(RegisterRequestDTO userRequest) {
        logger.info("Attempting to save user: {}", userRequest);

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmailAndProvider(userRequest.getEmail(), userRequest.getProvider());
        if (existingUser.isPresent()) {
            logger.error("User already exists with email: {} and provider: {}", userRequest.getEmail(), userRequest.getProvider());
            throw new IllegalArgumentException("User already exists with this email and provider.");
        }

        // Create and save the new user
        User newUser = new User();
        newUser.setName(userRequest.getName());
        newUser.setEmail(userRequest.getEmail());
        newUser.setProvider(userRequest.getProvider());

        if (newUser.getProvider() == User.Provider.LOCAL){
            newUser.setPasswordHash(userRequest.getPassword());
        }

        try {
            User savedUser = userRepository.save(newUser);
            logger.info("User saved successfully: {}", savedUser);
            return savedUser;
        } catch (Exception e) {
            logger.error("Error saving user: {}", e.getMessage(), e);
            throw e;
        }
    }
}
