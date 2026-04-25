package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.OrderRepository;
import com.bookstore.bookstore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public List<Order> getUserOrders(Integer userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order createOrder(Integer userId, Integer amount, Integer coinAmount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAmount(amount);
        order.setCoinAmount(coinAmount);
        order.setStatus("pending");
        return orderRepository.save(order);
    }

    @Transactional
    public Optional<Order> payOrder(Integer orderId, Integer userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId).orElse(null);
        if (order == null || !"pending".equals(order.getStatus())) {
            return Optional.empty();
        }
        order.setStatus("paid");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setCoins(user.getCoins() + order.getCoinAmount());
            userRepository.save(user);
        }
        return Optional.of(order);
    }
}
