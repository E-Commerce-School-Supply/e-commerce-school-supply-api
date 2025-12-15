package com.e_commerce.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId; // user email or id

    private List<OrderItem> items = new ArrayList<>();

    private Double total;

    private Object address;
    private Object payment;
    private String shipping;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    public static class OrderItem {
        private String productId;
        private String name;
        private Double price;
        private Integer quantity;
        private String image;
    }
}
