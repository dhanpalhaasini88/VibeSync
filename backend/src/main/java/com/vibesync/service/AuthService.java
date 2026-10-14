package com.vibesync.service;

import com.vibesync.config.JwtUtil;
import com.vibesync.entity.User;
import com.vibesync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // BCrypt from SecurityConfig

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    /**
     * Register a new user.
     * Returns a Map so the controller can return a clean JSON response.
     */
    public Map<String, Object> register(String name, String email, String rawPassword) {
        Map<String, Object> response = new HashMap<>();

        // 1. Check if email is already registered
        if (userRepository.existsByEmail(email)) {
            response.put("success", false);
            response.put("message", "Email is already registered. Please log in.");
            return response;
        }

        // 2. Hash the password with BCrypt before saving
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // 3. Create and save the new user
        User newUser = new User(name, email, hashedPassword);
        newUser.setRole(isConfiguredAdmin(email) ? "ADMIN" : "USER");
        userRepository.save(newUser);

        // 4. Generate a JWT token so they're logged in immediately after registering
        String token = jwtUtil.generateToken(email);

        response.put("success", true);
        response.put("message", "Account created successfully!");
        response.put("token", token);
        response.put("name", name);
        response.put("email", email);
        response.put("role", newUser.getRole());
        return response;
    }

    /**
     * Log in an existing user.
     * Returns a Map with token and user info on success, or an error message.
     */
    public Map<String, Object> login(String email, String rawPassword) {
        Map<String, Object> response = new HashMap<>();

        // 1. Find the user by email
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "No account found with this email.");
            return response;
        }

        User user = userOpt.get();

        // 2. Check if password matches the stored BCrypt hash
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            response.put("success", false);
            response.put("message", "Incorrect password.");
            return response;
        }

        if (isConfiguredAdmin(email)) {
            user.setRole("ADMIN");
            userRepository.save(user);
        } else if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
            userRepository.save(user);
        }

        // 3. Generate JWT token
        String token = jwtUtil.generateToken(email);

        response.put("success", true);
        response.put("message", "Login successful!");
        response.put("token", token);
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        return response;
    }

    private boolean isConfiguredAdmin(String email) {
        return adminEmail != null
                && !adminEmail.isBlank()
                && email != null
                && adminEmail.trim().equalsIgnoreCase(email.trim());
    }
}
