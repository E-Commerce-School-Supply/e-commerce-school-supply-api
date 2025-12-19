package com.e_commerce.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
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
    private String imageUrl;
    private String status;
    private Double averageRating;
    private Integer reviewCount;
}