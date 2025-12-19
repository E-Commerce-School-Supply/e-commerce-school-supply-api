package com.e_commerce.backend.repository;

import com.e_commerce.backend.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProductId(String productId);
    List<Review> findByUserId(String userId);
    Optional<Review> findByProductIdAndUserId(String productId, String userId);
    void deleteByProductId(String productId);
    long countByProductId(String productId);
}
