package com.e_commerce.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String productId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String title;
    private String body;
    private Integer rating; // 1-5 stars
    private Boolean recommend; // true/false
    private Boolean verified; // true if user purchased the product
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
