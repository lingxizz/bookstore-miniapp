package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.LoginRequest;
import com.bookstore.bookstore.dto.LoginResponse;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String code = request.getCode() != null ? request.getCode() : "mock_code";
        String openid;
        if (code.startsWith("h5_")) {
            String forwarded = httpRequest.getHeader("X-Forwarded-For");
            String clientIp = forwarded != null ? forwarded.split(",")[0].trim() : httpRequest.getRemoteAddr();
            openid = "h5_" + clientIp;
        } else {
            openid = "openid_" + code;
        }
        User user = authService.findOrCreateUser(openid, request.getNickname(), request.getAvatar());
        String token = authService.generateToken(user);
        return new LoginResponse(token, user);
    }
}
