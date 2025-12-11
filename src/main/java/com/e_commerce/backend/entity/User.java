package com.e_commerce.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String role;
    private String phoneNumber;
    private String avatarUrl;
    
    private List<Address> addresses = new ArrayList<>();
    private List<SavedCard> savedCards = new ArrayList<>();
    private List<String> wishlistProductIds = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLoginDate;

    @Data
    public static class Address {
        private String label; // "Home", "School"
        private String country;
        private String city;
        private String addressLine;
        private boolean isDefault;
    }

    @Data
    public static class SavedCard {
        private String cardName;
        private String cardNumber;
        private String CVV; // "4242"
        private String expiryDate;
    }
}
