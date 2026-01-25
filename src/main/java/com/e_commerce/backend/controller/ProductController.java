package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.Product;
import com.e_commerce.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Optional<Product> product = repository.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", "Product not found"));
        }
        return ResponseEntity.ok(product.get());
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file uploaded"));
        }

        try {
            // 1. Setup path: uploads/products/
            String folder = "uploads/products/";
            Path uploadPath = Paths.get(folder);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Generate unique name
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            
            // 3. Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. Return the URL to the frontend
            String imageUrl = "/products/" + filename;
            return ResponseEntity.ok(Map.of("url", imageUrl));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Upload failed: " + e.getMessage()));
        }
    }

    // --- 2. UPDATED ENDPOINT: Create Product (JSON Only) ---
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {
        // The frontend will send the "images" list inside the product object
        // So we just need to validate and save
        
        // Auto-calculate status based on stock
        if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
            product.setStatus("In Stock");
        } else {
            product.setStatus("Out of stock");
        }

        Product savedProduct = repository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Product productUpdates) {
        // 1. Find the existing product first
        Optional<Product> existingProductOpt = repository.findById(id);
        if (existingProductOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", "Product not found"));
        }

        Product existingProduct = existingProductOpt.get();

        // 2. Update fields ONLY if they are not null in the incoming request
        if (productUpdates.getName() != null) existingProduct.setName(productUpdates.getName());
        if (productUpdates.getDescription() != null) existingProduct.setDescription(productUpdates.getDescription());
        if (productUpdates.getMainCategory() != null) existingProduct.setMainCategory(productUpdates.getMainCategory());
        if (productUpdates.getSubCategory() != null) existingProduct.setSubCategory(productUpdates.getSubCategory());
        if (productUpdates.getType() != null) existingProduct.setType(productUpdates.getType());
        if (productUpdates.getSize() != null) existingProduct.setSize(productUpdates.getSize());
        if (productUpdates.getColor() != null) existingProduct.setColor(productUpdates.getColor());
        if (productUpdates.getMaterial() != null) existingProduct.setMaterial(productUpdates.getMaterial());
        if (productUpdates.getBrandName() != null) existingProduct.setBrandName(productUpdates.getBrandName());
        if (productUpdates.getPrice() != null) existingProduct.setPrice(productUpdates.getPrice());
        if (productUpdates.getDiscount() != null) existingProduct.setDiscount(productUpdates.getDiscount());
        
        // Only update images if the client actually sent a new list
        if (productUpdates.getImages() != null && !productUpdates.getImages().isEmpty()) {
            existingProduct.setImages(productUpdates.getImages());
        }

        // 3. Special Logic: Stock & Status
        if (productUpdates.getStockQuantity() != null) {
            existingProduct.setStockQuantity(productUpdates.getStockQuantity());
            // Recalculate status based on the NEW stock quantity
            if (existingProduct.getStockQuantity() > 0) {
                existingProduct.setStatus("In Stock");
            } else {
                existingProduct.setStatus("Out of stock");
            }
        }

        // Note: We intentionally do NOT update 'averageRating' or 'reviewCount' here
        // so they preserve their existing values from the database.

        // 4. Save the merged object
        Product savedProduct = repository.save(existingProduct);
        return ResponseEntity.ok(savedProduct);
    }
}