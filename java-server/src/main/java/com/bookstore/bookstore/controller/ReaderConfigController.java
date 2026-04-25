package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.ReaderConfigRequest;
import com.bookstore.bookstore.entity.ReaderConfig;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.ReaderConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class ReaderConfigController {

    private final ReaderConfigService readerConfigService;

    public ReaderConfigController(ReaderConfigService readerConfigService) {
        this.readerConfigService = readerConfigService;
    }

    @GetMapping("/reader-config")
    public ResponseEntity<?> getConfig(Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        ReaderConfig config = readerConfigService.getConfig(userId).orElse(null);
        if (config == null) {
            return ResponseEntity.ok(Map.of(
                "fontSize", 18,
                "lineHeight", 160,
                "theme", "light",
                "brightness", 100
            ));
        }
        return ResponseEntity.ok(config);
    }

    @PostMapping("/reader-config")
    public ResponseEntity<?> saveConfig(@RequestBody ReaderConfigRequest request, Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        ReaderConfig config = readerConfigService.saveConfig(userId, request.getFontSize(), request.getLineHeight(), request.getTheme(), request.getBrightness());
        return ResponseEntity.ok(config);
    }
}
