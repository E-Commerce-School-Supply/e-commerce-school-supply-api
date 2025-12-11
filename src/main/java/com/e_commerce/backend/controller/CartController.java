package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.Cart;
import com.e_commerce.backend.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    /**
     * Get the current user's cart
     */
    @GetMapping
    public ResponseEntity<?> getCart() {
        String userId = getCurrentUserId();
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        
        if (cart.isPresent()) {
            return ResponseEntity.ok(cart.get());
        }
        
        // Create a new cart if it doesn't exist
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        Cart savedCart = cartRepository.save(newCart);
        return ResponseEntity.ok(savedCart);
    }

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart(@RequestBody Cart.CartItem item) {
        String userId = getCurrentUserId();
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        
        Cart cart;
        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
        } else {
            cart = new Cart();
            cart.setUserId(userId);
        }

        // Check if item already exists in cart
        Optional<Cart.CartItem> existingItem = cart.getItems()
                .stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // If item exists, increase quantity
            existingItem.get().setQuantity(existingItem.get().getQuantity() + item.getQuantity());
        } else {
            // Add new item to cart
            cart.getItems().add(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart updatedCart = cartRepository.save(cart);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Update item quantity in cart
     */
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable String productId,
            @RequestBody Map<String, Integer> request) {
        
        String userId = getCurrentUserId();
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        
        if (!cartOptional.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "Cart not found"));
        }

        Cart cart = cartOptional.get();
        Integer newQuantity = request.get("quantity");

        Optional<Cart.CartItem> itemToUpdate = cart.getItems()
                .stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (itemToUpdate.isPresent()) {
            if (newQuantity > 0) {
                itemToUpdate.get().setQuantity(newQuantity);
            } else {
                // Remove item if quantity is 0 or less
                cart.getItems().removeIf(i -> i.getProductId().equals(productId));
            }
            cart.setUpdatedAt(LocalDateTime.now());
            Cart updatedCart = cartRepository.save(cart);
            return ResponseEntity.ok(updatedCart);
        }

        return ResponseEntity.status(404).body(Map.of("message", "Item not found in cart"));
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable String productId) {
        String userId = getCurrentUserId();
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        
        if (!cartOptional.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "Cart not found"));
        }

        Cart cart = cartOptional.get();
        boolean removed = cart.getItems().removeIf(i -> i.getProductId().equals(productId));

        if (removed) {
            cart.setUpdatedAt(LocalDateTime.now());
            Cart updatedCart = cartRepository.save(cart);
            return ResponseEntity.ok(updatedCart);
        }

        return ResponseEntity.status(404).body(Map.of("message", "Item not found in cart"));
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        String userId = getCurrentUserId();
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            cart.getItems().clear();
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
        
        return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
    }

    /**
     * Helper method to get current authenticated user's email from JWT token
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        // The principal is a UserDetails object, get the username (email)
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername(); // Returns email
        }
        return authentication.getPrincipal().toString();
    }
}
