package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.User;
import com.e_commerce.backend.repository.UserRepository;
import com.e_commerce.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use!"));
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already taken!"));
        }

        // ENCODE PASSWORD BEFORE SAVING
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER"); // Default role
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email"); 
        String password = loginRequest.get("password");

        try {
            // This authenticates using Spring Security Manager (checks DB and BCrypt password)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // If successful, generate token
            if (authentication.isAuthenticated()) {
                String token = jwtUtil.generateToken(email);
                User userDetails = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Construct the Response Map
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("token", token);
                
                // User Identity
                response.put("id", userDetails.getId());
                response.put("username", userDetails.getUsername());
                response.put("email", userDetails.getEmail());
                response.put("role", userDetails.getRole());
                response.put("phoneNumber", userDetails.getPhoneNumber());
                
                // // New Fields
                // response.put("createdAt", userDetails.getCreatedAt());
                
                // // Lists (JSON arrays)
                // response.put("addresses", userDetails.getAddresses());
                // response.put("savedCards", userDetails.getSavedCards()); 
                // response.put("wishlistProductIds", userDetails.getWishlistProductIds());
                

                
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
        return ResponseEntity.status(401).body(Map.of("message", "Authentication failed"));
    }
}