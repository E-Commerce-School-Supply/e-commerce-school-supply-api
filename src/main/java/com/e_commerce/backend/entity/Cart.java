package com.e_commerce.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Data;

@Data
@Document(collection = "carts")
public class Cart {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Reference to User ID

    private List<CartItem> items = new ArrayList<>();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    public static class CartItem {
        private String productId;
        private String name;
        private String itemNo;
        private String brand;
        private String color;
        private Double rating;
        private Double price;
        private Integer quantity;
        private List<String> images = new ArrayList<>();
    }
}