package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.Product;
import com.e_commerce.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @PostMapping
    public Product create(@RequestBody Product product) {
        // Auto-calculate status based on stock
        if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
            product.setStatus("In Stock");
        } else {
            product.setStatus("Out of stock");
        }
        return repository.save(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable String id, @RequestBody Product product) {
        product.setId(id);
        // Auto-calculate status based on stock
        if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
            product.setStatus("In Stock");
        } else {
            product.setStatus("Out of stock");
        }
        Product updated = repository.save(product);
        return ResponseEntity.ok(updated);
    }
}