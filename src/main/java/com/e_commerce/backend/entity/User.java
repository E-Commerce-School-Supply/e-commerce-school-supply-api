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
    private boolean deactivated = false;
    
    private List<Address> addresses = new ArrayList<>();
    private List<SavedCard> savedCards = new ArrayList<>();
    private List<String> wishlistProductIds = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLoginDate;

    @Data
    public static class Address {
        private Long id;
        private String label; // "Home", "School"
        private String country;
        private String city;
        private String houseNumber;
        private String street;
        private String addressLine;
        private String addressLine2;
        private String zipCode;
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
