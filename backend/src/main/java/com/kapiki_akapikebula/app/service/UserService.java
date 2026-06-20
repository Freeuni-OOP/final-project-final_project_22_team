package com.kapiki_akapikebula.app.service;

import com.kapiki_akapikebula.app.model.User;
import com.kapiki_akapikebula.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("A user with this email address already exists!");
        }

        String hashedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    public User loginUser(String email, String password) {
        // 1. ვეძებთ იუზერს იმეილით
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("A user with this email not found!"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("incorrect password!");
        }
        return user;
    }
}