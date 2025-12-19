package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.User;
import com.e_commerce.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String getCurrentEmail() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) return null;
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile() {
        String email = getCurrentEmail();
        if (email == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        // Do not return password - use HashMap to handle null values
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("avatarUrl", user.getAvatarUrl());
        response.put("addresses", user.getAddresses());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> payload) {
        String email = getCurrentEmail();
        if (email == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        // Validate email/username uniqueness if changed
        if (payload.containsKey("email")) {
            String newEmail = (String) payload.get("email");
            if (newEmail != null && !newEmail.equals(user.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already in use"));
                }
                user.setEmail(newEmail);
            }
        }
        if (payload.containsKey("username")) {
            String newUsername = (String) payload.get("username");
            if (newUsername != null && !newUsername.equals(user.getUsername())) {
                if (userRepository.findByUsername(newUsername).isPresent()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already in use"));
                }
                user.setUsername(newUsername);
            }
        }

        if (payload.containsKey("phoneNumber")) user.setPhoneNumber((String) payload.get("phoneNumber"));
        if (payload.containsKey("avatarUrl")) user.setAvatarUrl((String) payload.get("avatarUrl"));

        // Support updating addresses if provided
        if (payload.containsKey("addresses")) {
            try {
                Object raw = payload.get("addresses");
                if (raw instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) raw;
                    user.getAddresses().clear();
                    for (Object o : list) {
                        if (o instanceof java.util.Map) {
                            java.util.Map<?, ?> m = (java.util.Map<?, ?>) o;
                            User.Address a = new User.Address();
                            Object id = m.get("id");
                            Object label = m.get("label");
                            Object city = m.get("city");
                            Object country = m.get("country");
                            Object houseNumber = m.get("houseNumber");
                            Object street = m.get("street");
                            Object addressLine = m.get("addressLine");
                            Object addressLine2 = m.get("addressLine2");
                            Object zipCode = m.get("zipCode");
                            Object isDefault = m.get("isDefault");
                            if (id != null) {
                                try {
                                    a.setId(Long.parseLong(id.toString()));
                                } catch (NumberFormatException e) {
                                    // ignore invalid id
                                }
                            }
                            if (label != null) a.setLabel(label.toString());
                            if (city != null) a.setCity(city.toString());
                            if (country != null) a.setCountry(country.toString());
                            if (houseNumber != null) a.setHouseNumber(houseNumber.toString());
                            if (street != null) a.setStreet(street.toString());
                            if (addressLine != null) a.setAddressLine(addressLine.toString());
                            if (addressLine2 != null) a.setAddressLine2(addressLine2.toString());
                            if (zipCode != null) a.setZipCode(zipCode.toString());
                            if (isDefault != null) a.setDefault(Boolean.parseBoolean(isDefault.toString()));
                            user.getAddresses().add(a);
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore malformed addresses
            }
        }

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String email = getCurrentEmail();
        if (email == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file uploaded"));
        }

        try {
            // Store uploaded avatars in a consistent, absolute runtime-writable folder
            Path uploadDir = Paths.get("uploads", "avatars").toAbsolutePath();
            Files.createDirectories(uploadDir);

            String original = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "-" + (original != null ? original.replaceAll("[^a-zA-Z0-9.\\-]", "_") : "avatar.jpg");
            Path target = uploadDir.resolve(filename);

            // Ensure parent directories exist before writing
            Files.createDirectories(target.getParent());

            // Use Files.copy with the MultipartFile input stream for a reliable write
            try (java.io.InputStream in = file.getInputStream()) {
                java.nio.file.Files.copy(in, target);
            }

            // Build a full public URL (includes scheme, host and port) so frontend can load it directly
            String publicUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/avatars/")
                    .path(filename)
                    .toUriString();

            user.setAvatarUrl(publicUrl);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("avatarUrl", publicUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to save file"));
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String email = getCurrentEmail();
        if (email == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) return ResponseEntity.badRequest().body(Map.of("message", "Missing passwords"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(403).body(Map.of("message", "Old password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed"));
    }
}
