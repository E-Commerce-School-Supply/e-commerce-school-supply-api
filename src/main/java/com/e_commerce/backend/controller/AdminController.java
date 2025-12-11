package com.e_commerce.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.e_commerce.backend.dto.UserResponseDTO;
import com.e_commerce.backend.entity.User;
import com.e_commerce.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all users with their details
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(Authentication authentication) {
        try {
            // Debug: Check if user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("Authentication is null or not authenticated");
                return ResponseEntity.status(403).build();
            }
            
            System.out.println("Authenticated user: " + authentication.getName());
            
            List<User> users = userRepository.findAll();
            List<UserResponseDTO> userDTOs = users.stream()
                    .filter(user -> !"ADMIN".equals(user.getRole())) // Hide admin users
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            System.out.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get total user count
     */
    @GetMapping("/users/count")
    public ResponseEntity<Long> getUserCount(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("Authentication is null or not authenticated");
                return ResponseEntity.status(403).build();
            }
            
            long count = userRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            System.out.println("Error fetching user count: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Convert User entity to UserResponseDTO
     */
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());
        
        // Set status as Active
        dto.setStatus("Active");
        
        // Format last login date in a user-friendly format
        if (user.getLastLoginDate() != null) {
            dto.setLastLoginDate(formatDate(user.getLastLoginDate()));
        } else {
            // If no last login date, use creation date
            dto.setLastLoginDate(user.getCreatedAt() != null ? formatDate(user.getCreatedAt()) : "Never");
        }
        
        return dto;
    }
    
    /**
     * Format LocalDateTime to a user-friendly string
     * Examples: "Just now", "5 minutes ago", "2 hours ago", "Jan 15, 2025"
     * Uses Cambodia timezone (UTC+7)
     */
    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        
        try {
            // Use Phnom Penh timezone (Cambodia UTC+7)
            java.time.ZoneId cambodiaZone = java.time.ZoneId.of("Asia/Phnom_Penh");
            
            // Convert stored time to Instant assuming it was stored in UTC, then to Cambodia time
            java.time.ZonedDateTime storedZoned = dateTime.atZone(java.time.ZoneId.of("UTC")).withZoneSameInstant(cambodiaZone);
            java.time.LocalDateTime storedInCambodia = storedZoned.toLocalDateTime();
            java.time.LocalDateTime nowInCambodia = java.time.LocalDateTime.now(cambodiaZone);
            
            long minutesDiff = java.time.temporal.ChronoUnit.MINUTES.between(storedInCambodia, nowInCambodia);
            long hoursDiff = java.time.temporal.ChronoUnit.HOURS.between(storedInCambodia, nowInCambodia);
            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(storedInCambodia, nowInCambodia);
            
            if (minutesDiff < 1) {
                return "Just now";
            } else if (minutesDiff < 60) {
                return minutesDiff + " minute" + (minutesDiff == 1 ? "" : "s") + " ago";
            } else if (hoursDiff < 24) {
                return hoursDiff + " hour" + (hoursDiff == 1 ? "" : "s") + " ago";
            } else if (daysDiff < 7) {
                return daysDiff + " day" + (daysDiff == 1 ? "" : "s") + " ago";
            } else {
                // Format as "MMM dd, yyyy" in Cambodia timezone
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Phnom_Penh"));
                return sdf.format(java.sql.Timestamp.valueOf(storedInCambodia));
            }
        } catch (Exception e) {
            // Fallback to simple format if any error occurs
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
            return sdf.format(java.sql.Timestamp.valueOf(dateTime));
        }
    }
}
