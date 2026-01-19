package com.e_commerce.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

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

        // New list-based storage for multiple images
        private List<String> images = new ArrayList<>();

        // Legacy single image field kept for backward compatibility
        private String image;

        // Prefer first list entry when `image` is not set
        public String getImage() {
            if (image != null && !image.isBlank()) return image;
            return images == null || images.isEmpty() ? null : images.get(0);
        }

        // Accept legacy payloads and also seed the list for consistency
        public void setImage(String image) {
            this.image = image;
            if (image == null || image.isBlank()) return;
            if (images == null) images = new ArrayList<>();
            if (images.isEmpty()) images.add(image);
        }

        // Allow clients to send `image` key while they migrate to `images`
        @JsonProperty("image")
        public void setLegacyImage(String image) {
            setImage(image);
        }

        @JsonProperty("image")
        public String getLegacyImage() {
            return getImage();
        }

        public void setImages(List<String> images) {
            this.images = images == null ? new ArrayList<>() : new ArrayList<>(images);
        }

        public List<String> getImages() {
            return images == null ? Collections.emptyList() : images;
        }
    }
}
