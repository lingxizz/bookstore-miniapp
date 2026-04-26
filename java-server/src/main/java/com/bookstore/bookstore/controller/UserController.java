package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public User me(Authentication authentication) {
        if (authentication instanceof JwtAuthentication jwtAuth) {
            return userService.getUserById(jwtAuth.getUserId()).orElse(null);
        }
        return null;
    }

    @GetMapping("/balance")
    public Map<String, Object> balance(Authentication authentication) {
        if (authentication instanceof JwtAuthentication jwtAuth) {
            Integer coins = userService.getUserById(jwtAuth.getUserId())
                    .map(User::getCoins).orElse(0);
            return Map.of("balance", (Object) coins);
        }
        return Map.of("balance", (Object) 0);
    }
}
