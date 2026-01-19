package com.e_commerce.backend.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id; 
    private String name;
    private String description;
    private String mainCategory;
    private String subCategory;
    private String type;
    private String size;
    private String color;
    private String material;
    private String brandName;
    private Integer stockQuantity;
    private Double price;
    private Double discount;

    // Support multiple images; defaults to empty list to avoid null handling on the client.
    private List<String> images = new ArrayList<>();

    // Backward compatibility: allow old payloads using imageUrl to populate the images list
    @JsonProperty("imageUrl")
    public void setLegacyImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(imageUrl);
    }

    // Backward compatibility: expose the first image as imageUrl for older clients
    @JsonProperty("imageUrl")
    public String getLegacyImageUrl() {
        return (images == null || images.isEmpty()) ? null : images.get(0);
    }

    private String status;
    private Double averageRating;
    private Integer reviewCount;
}