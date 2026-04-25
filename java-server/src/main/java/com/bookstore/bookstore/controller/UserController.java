package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
