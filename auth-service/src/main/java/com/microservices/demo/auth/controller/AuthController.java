package com.microservices.demo.auth.controller;

import com.microservices.demo.auth.dto.LoginRequest;
import com.microservices.demo.auth.dto.LoginResponse;
import com.microservices.demo.auth.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    // Mock users (no database)
    private static final Map<String, String> MOCK_USERS = Map.of(
            "admin", "admin123",
            "user", "user123",
            "demo", "demo123"
    );

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // Mock authentication
        if (MOCK_USERS.containsKey(username) && MOCK_USERS.get(username).equals(password)) {
            String token = jwtUtil.generateToken(username);
            LoginResponse response = new LoginResponse(token, username, "Login successful!");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password"));
    }
}
