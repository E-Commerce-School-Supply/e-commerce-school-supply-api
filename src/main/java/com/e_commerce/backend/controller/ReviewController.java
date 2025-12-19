package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.Review;
import com.e_commerce.backend.entity.Product;
import com.e_commerce.backend.repository.ReviewRepository;
import com.e_commerce.backend.repository.ProductRepository;
import com.e_commerce.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Get all reviews for a product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    // Get all reviews by a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable String userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    // Get user's review for a specific product
    @GetMapping("/product/{productId}/user/{userId}")
    public ResponseEntity<?> getUserReviewForProduct(
            @PathVariable String productId,
            @PathVariable String userId) {
        Optional<Review> review = reviewRepository.findByProductIdAndUserId(productId, userId);
        if (review.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Review not found"));
        }
        return ResponseEntity.ok(review.get());
    }

    // Create or update a review
    @PostMapping("/product/{productId}")
    public ResponseEntity<?> createOrUpdateReview(
            @PathVariable String productId,
            @RequestBody Review review,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Extract token and get user ID
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUsername(token);

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid token"));
            }

            // Verify product exists
            Optional<Product> product = productRepository.findById(productId);
            if (product.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Product not found"));
            }

            // Check if user already has a review for this product
            Optional<Review> existingReview = reviewRepository.findByProductIdAndUserId(productId, userId);

            Review savedReview;
            if (existingReview.isPresent()) {
                // Update existing review
                Review toUpdate = existingReview.get();
                toUpdate.setTitle(review.getTitle());
                toUpdate.setBody(review.getBody());
                toUpdate.setRating(review.getRating());
                toUpdate.setRecommend(review.getRecommend());
                toUpdate.setUpdatedAt(LocalDateTime.now());
                savedReview = reviewRepository.save(toUpdate);
            } else {
                // Create new review
                review.setId(null);
                review.setProductId(productId);
                review.setUserId(userId);
                review.setCreatedAt(LocalDateTime.now());
                review.setUpdatedAt(LocalDateTime.now());
                savedReview = reviewRepository.save(review);
            }

            // Update product rating statistics
            updateProductRating(productId);

            return ResponseEntity.ok(savedReview);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error saving review: " + e.getMessage()));
        }
    }

    // Delete a review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable String reviewId,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUsername(token);

            Optional<Review> review = reviewRepository.findById(reviewId);
            if (review.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Review not found"));
            }

            // Check if user owns this review
            if (!review.get().getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("message", "Not authorized to delete this review"));
            }

            String productId = review.get().getProductId();
            reviewRepository.deleteById(reviewId);

            // Update product rating statistics
            updateProductRating(productId);

            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting review: " + e.getMessage()));
        }
    }

    // Admin: Delete a review by ID
    @DeleteMapping("/admin/{reviewId}")
    public ResponseEntity<?> adminDeleteReview(@PathVariable String reviewId) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Review not found"));
        }

        String productId = review.get().getProductId();
        reviewRepository.deleteById(reviewId);

        // Update product rating statistics
        updateProductRating(productId);

        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }

    // Admin: Get all reviews
    @GetMapping("/admin/all")
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return ResponseEntity.ok(reviews);
    }

    // Helper method to update product rating statistics
    private void updateProductRating(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            if (reviews.isEmpty()) {
                product.setAverageRating(0.0);
                product.setReviewCount(0);
            } else {
                double avgRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
                product.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
                product.setReviewCount(reviews.size());
            }
            
            productRepository.save(product);
        }
    }
}
