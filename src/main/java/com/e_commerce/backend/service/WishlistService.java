package com.e_commerce.backend.service;

import com.e_commerce.backend.entity.Product;
import com.e_commerce.backend.entity.User;
import com.e_commerce.backend.repository.ProductRepository;
import com.e_commerce.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> productIds = user.getWishlistProductIds();
        
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch all products matching the IDs in the user's wishlist
        return productRepository.findAllById(productIds);
    }

    public void addToWishlist(String email, String productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getWishlistProductIds() == null) {
            user.setWishlistProductIds(new java.util.ArrayList<>());
        }

        // Prevent duplicates
        if (!user.getWishlistProductIds().contains(productId)) {
            user.getWishlistProductIds().add(productId);
            userRepository.save(user);
        }
    }

    public void removeFromWishlist(String email, String productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getWishlistProductIds() != null) {
            user.getWishlistProductIds().remove(productId);
            userRepository.save(user);
        }
    }
}