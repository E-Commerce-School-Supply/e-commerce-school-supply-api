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

    }
}
