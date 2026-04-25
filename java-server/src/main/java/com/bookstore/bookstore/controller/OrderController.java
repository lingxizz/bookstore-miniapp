package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.CreateOrderRequest;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order createOrder(@RequestBody CreateOrderRequest request, Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        return orderService.createOrder(userId, request.getAmount(), request.getCoinAmount());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Integer id, Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        return orderService.payOrder(id, userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("error", "Order not found")));
    }
}
