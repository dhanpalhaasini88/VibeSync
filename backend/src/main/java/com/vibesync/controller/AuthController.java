package com.vibesync.controller;

import com.vibesync.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController — handles user registration and login.
 *
 * Routes (all public — no JWT needed):
 *   POST /api/auth/register  → create a new account
 *   POST /api/auth/login     → login and receive a JWT token
 *
 * Request body example:
 *   { "name": "Haasi", "email": "haasi@example.com", "password": "secret123" }
 *
 * Response example (success):
 *   { "success": true, "token": "eyJ...", "name": "Haasi", "email": "haasi@example.com" }
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String name     = body.get("name");
        String email    = body.get("email");
        String password = body.get("password");

        // Basic validation
        if (name == null || email == null || password == null ||
            name.isBlank() || email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Name, email, and password are required.")
            );
        }

        Map<String, Object> result = authService.register(name, email, password);
        boolean success = (boolean) result.get("success");
        return success
            ? ResponseEntity.ok(result)
            : ResponseEntity.badRequest().body(result);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Email and password are required.")
            );
        }

        Map<String, Object> result = authService.login(email, password);
        boolean success = (boolean) result.get("success");
        return success
            ? ResponseEntity.ok(result)
            : ResponseEntity.status(401).body(result);
    }
}
