package com.org.project.service;

import com.org.project.model.User;
import com.org.project.model.auth.RegisterRequestDTO;
import com.org.project.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User registerUser(RegisterRequestDTO userRequest) {
        Optional<User> existingUser = userRepository.findByEmailAndProvider(userRequest.getEmail(), userRequest.getProvider());

        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User already exists with this email and provider.");
        }

        User newUser = new User();
        newUser.setName(userRequest.getName());
        newUser.setEmail(userRequest.getEmail());
        newUser.setProvider(userRequest.getProvider());

        if (newUser.getProvider() == User.Provider.LOCAL){
            newUser.setPasswordHash(userRequest.getPassword());
        }

        try {
            return userRepository.save(newUser);
        } catch (Exception e) {
            throw e;
        }
    }

    public User getUserFromId(String userId) {
        return userRepository.findById(userId);
    }
}
