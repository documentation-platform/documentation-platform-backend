package com.org.project.service;

import com.org.project.model.User;
import com.org.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User saveUser(User user) {
        logger.info("Attempting to save user: {}", user);
        try {
            User savedUser = userRepository.save(user);
            logger.info("User saved successfully: {}", savedUser);
            return savedUser;
        } catch (Exception e) {
            logger.error("Error saving user: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }
}