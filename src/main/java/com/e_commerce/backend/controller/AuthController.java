package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.User;
import com.e_commerce.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // Allow Vue frontend
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already in use!"));
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken!"));
        }

        // In a real app, encode the password here!
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // In a real app, check encoded password
            if (user.getPassword().equals(password)) {
                return ResponseEntity.ok(Map.of(
                    "message", "Login successful!",
                    "username", user.getUsername(),
                    "email", user.getEmail()
                ));
            }
        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
    }
}
