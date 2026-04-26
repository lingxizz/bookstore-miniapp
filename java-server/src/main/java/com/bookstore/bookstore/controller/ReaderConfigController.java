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
        ReaderConfig config = null;
        if (authentication instanceof JwtAuthentication jwtAuth) {
            config = readerConfigService.getConfig(jwtAuth.getUserId()).orElse(null);
        }
        if (config == null) {
            return ResponseEntity.ok(Map.of(
                "fontSize", 18,
                "lineHeight", 160,
                "theme", "light",
                "brightness", 100,
                "paragraphSpacing", 24,
                "fontFamily", "serif",
                "pagingMode", "scroll"
            ));
        }
        return ResponseEntity.ok(config);
    }

    @PostMapping("/reader-config")
    public ResponseEntity<?> saveConfig(@RequestBody ReaderConfigRequest request, Authentication authentication) {
        if (!(authentication instanceof JwtAuthentication jwtAuth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        ReaderConfig config = readerConfigService.saveConfig(jwtAuth.getUserId(), request);
        return ResponseEntity.ok(config);
    }
}
