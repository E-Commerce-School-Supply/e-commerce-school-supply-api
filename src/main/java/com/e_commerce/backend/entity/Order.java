package com.e_commerce.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.e_commerce.backend.enums.OrderStatus;
import com.e_commerce.backend.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId; // user email or id

    @Transient
    private User user; // Populated for admin views, not stored in DB

    private List<OrderItem> items = new ArrayList<>();

    private Double total;

    private Object address;
    private Object payment;
    private String shipping;
    private PaymentMethod paymentMethod = PaymentMethod.CARD; 
    private OrderStatus status = OrderStatus.PENDING;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    public static class OrderItem {
        private String productId;
        private String name;
        private Double price;
        private Integer quantity;

        // New list-based storage for multiple images
        private List<String> images = new ArrayList<>();

        // Legacy single image field kept for backward compatibility
        private String image;

        public String getImage() {
            if (image != null && !image.isBlank()) return image;
            return images == null || images.isEmpty() ? null : images.get(0);
        }

        public void setImage(String image) {
            this.image = image;
            if (image == null || image.isBlank()) return;
            if (images == null) images = new ArrayList<>();
            if (images.isEmpty()) images.add(image);
        }

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
