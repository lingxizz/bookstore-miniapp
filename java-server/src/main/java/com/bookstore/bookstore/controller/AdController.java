package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.AdUnlockRequest;
import com.bookstore.bookstore.dto.SuccessResponse;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.AdService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ad")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @PostMapping("/complete")
    public ResponseEntity<?> adComplete(Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        String token = adService.generateAdToken(userId);
        if (token == null) {
            return ResponseEntity.ok(Map.of("error", "Daily ad limit reached"));
        }
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> adUnlock(@RequestBody AdUnlockRequest request, Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        boolean success = adService.verifyAdToken(userId, request.getChapterId(), request.getAdToken());
        if (!success) {
            return ResponseEntity.ok(Map.of("error", "Invalid or expired ad token"));
        }
        return ResponseEntity.ok(new SuccessResponse());
    }
}
