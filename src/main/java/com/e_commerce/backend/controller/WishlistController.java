package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.Product;
import com.e_commerce.backend.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:5173")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // Helper to get current logged-in user's email from JWT
    private String getCurrentEmail() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) return null;
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    public ResponseEntity<?> getWishlist() {
        String email = getCurrentEmail();
        if (email == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        List<Product> wishlist = wishlistService.getWishlist(email);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> addToWishlist(@PathVariable String productId) {
        String email = getCurrentEmail();
        if (email == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        wishlistService.addToWishlist(email, productId);
        return ResponseEntity.ok(Map.of("message", "Product added to wishlist"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable String productId) {
        String email = getCurrentEmail();
        if (email == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        wishlistService.removeFromWishlist(email, productId);
        return ResponseEntity.ok(Map.of("message", "Product removed from wishlist"));
    }
}