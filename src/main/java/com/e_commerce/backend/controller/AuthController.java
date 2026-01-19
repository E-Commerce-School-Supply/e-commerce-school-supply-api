package com.e_commerce.backend.controller;

import com.e_commerce.backend.dto.SigninRequestDTO;
import com.e_commerce.backend.dto.SignupRequestDTO;
import com.e_commerce.backend.entity.User;
import com.e_commerce.backend.repository.UserRepository;
import com.e_commerce.backend.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signupRequest, BindingResult bindingResult) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", errors));
        }

        // Validate passwords match
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match!"));
        }

        // Check if email already exists
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use!"));
        }

        // Check if username already exists
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already taken!"));
        }

        // Create new user
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setRole("USER"); // Default role
        
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SigninRequestDTO signinRequest, BindingResult bindingResult) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", errors));
        }

        try {
            // This authenticates using Spring Security Manager (checks DB and BCrypt password)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signinRequest.getEmail(), signinRequest.getPassword())
            );

            // If successful, generate token
            if (authentication.isAuthenticated()) {
                String token = jwtUtil.generateToken(signinRequest.getEmail());
                User userDetails = userRepository.findByEmail(signinRequest.getEmail())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Update last login date (using Phnom Penh timezone UTC+7)
                java.time.ZoneId cambodiaZone = java.time.ZoneId.of("Asia/Phnom_Penh");
                userDetails.setLastLoginDate(java.time.LocalDateTime.now(cambodiaZone));
                userRepository.save(userDetails);

                // Construct the Response Map
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                
                // User Identity
                response.put("id", userDetails.getId());
                response.put("username", userDetails.getUsername());
                response.put("email", userDetails.getEmail());
                response.put("role", userDetails.getRole());
                response.put("phoneNumber", userDetails.getPhoneNumber());
                // Include avatar URL so the frontend can display the user's avatar immediately after login
                response.put("avatarUrl", userDetails.getAvatarUrl());
                
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