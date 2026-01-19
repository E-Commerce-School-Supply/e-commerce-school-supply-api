package com.e_commerce.backend.controller;

import com.e_commerce.backend.entity.Cart;
import com.e_commerce.backend.entity.Order;
import com.e_commerce.backend.entity.Product;
import com.e_commerce.backend.enums.OrderStatus;
import com.e_commerce.backend.enums.PaymentMethod;
import com.e_commerce.backend.repository.CartRepository;
import com.e_commerce.backend.repository.OrderRepository;
import com.e_commerce.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Create an order and reduce stock for ordered products.
     * This endpoint will check product availability and decrement stock accordingly.
     * It accepts an optional `cartId` in the payload so the frontend can call this
     * endpoint without authentication during local testing.
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        // First, try to locate cart by provided cartId (frontend supplies this for unauthenticated tests)
        Optional<Cart> cartOpt = Optional.empty();
        if (payload != null && payload.containsKey("cartId") && payload.get("cartId") != null) {
            String cartId = String.valueOf(payload.get("cartId"));
            cartOpt = cartRepository.findById(cartId);
        }

        // If cart not found by id, try by authenticated user
        if (cartOpt.isEmpty()) {
            try {
                String userId = getCurrentUserId();
                if (userId != null) {
                    cartOpt = cartRepository.findByUserId(userId);
                }
            } catch (Exception ignored) {
                // ignore - user not authenticated
            }
        }

        if (cartOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Cart not found"));
        }

        Cart cart = cartOpt.get();

        if (cart.getItems().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Cart is empty"));
        }

        // Validate stock
        for (Cart.CartItem item : cart.getItems()) {
            Optional<Product> pOpt = productRepository.findById(item.getProductId());
            if (!pOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("message", "Product not found: " + item.getProductId()));
            }
            Product p = pOpt.get();
            Integer qty = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
            if (qty < item.getQuantity()) {
                return ResponseEntity.status(400).body(Map.of("message", "Insufficient stock for product " + p.getName()));
            }
        }

        // Decrement stock
        for (Cart.CartItem item : cart.getItems()) {
            Optional<Product> pOpt = productRepository.findById(item.getProductId());
            if (pOpt.isPresent()) {
                Product p = pOpt.get();
                int newQty = (p.getStockQuantity() == null ? 0 : p.getStockQuantity()) - item.getQuantity();
                p.setStockQuantity(newQty);
                productRepository.save(p);
            }
        }

        // Build and persist order
        Order order = new Order();
        String currentUser = getCurrentUserId();
        if (currentUser != null) order.setUserId(currentUser);

        double total = 0.0;
        for (Cart.CartItem item : cart.getItems()) {
            Order.OrderItem oi = new Order.OrderItem();
            oi.setProductId(item.getProductId());
            oi.setName(item.getName());
            oi.setPrice(item.getPrice());
            oi.setQuantity(item.getQuantity());
            // Copy multi-image list; legacy single image falls back to first entry
            oi.setImages(new ArrayList<>(Optional.ofNullable(item.getImages()).orElse(Collections.emptyList())));
            order.getItems().add(oi);
            total += (item.getPrice() == null ? 0.0 : item.getPrice()) * (item.getQuantity() == null ? 0 : item.getQuantity());
        }

        order.setTotal(total);
        if (payload != null && payload.containsKey("address")) order.setAddress(payload.get("address"));
        if (payload != null && payload.containsKey("payment")) order.setPayment(payload.get("payment"));
        if (payload != null && payload.containsKey("shipping")) order.setShipping(String.valueOf(payload.get("shipping")));
        if (payload != null && payload.containsKey("paymentMethod")) {
            try {
                String pmStr = String.valueOf(payload.get("paymentMethod")).toUpperCase();
                order.setPaymentMethod(PaymentMethod.valueOf(pmStr));
            } catch (Exception e) {
                // Handle invalid payment method string if necessary
            }
        }
        if (payload != null && payload.containsKey("status")) {
            try {
                String statusStr = String.valueOf(payload.get("status")).toUpperCase();
                order.setStatus(OrderStatus.valueOf(statusStr));
            } catch (Exception e) {
                // Fallback or handle error
                order.setStatus(OrderStatus.PENDING);
            }
        }
        orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return ResponseEntity.ok(Map.of("message", "Order created successfully", "orderId", order.getId()));
    }

    @GetMapping
    public ResponseEntity<?> getOrders() {
        String userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        java.util.List<Order> orders = orderRepository.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable String id) {
        String userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Order not found"));
        }

        Order order = opt.get();
        // Only allow owner to view the order
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        return ResponseEntity.ok(order);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return authentication.getPrincipal().toString();
    }
}
