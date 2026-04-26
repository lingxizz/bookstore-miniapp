package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.SuccessResponse;
import com.bookstore.bookstore.entity.BookShelf;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.ShelfService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @PostMapping("/books/{id}/shelf")
    public ResponseEntity<?> addToShelf(@PathVariable Integer id, Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        shelfService.addToShelf(userId, id);
        return ResponseEntity.ok(new SuccessResponse());
    }

    @DeleteMapping("/books/{id}/shelf")
    public ResponseEntity<?> removeFromShelf(@PathVariable Integer id, Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        shelfService.removeFromShelf(userId, id);
        return ResponseEntity.ok(new SuccessResponse());
    }

    @GetMapping("/books/{id}/shelf")
    public ResponseEntity<?> checkShelf(@PathVariable Integer id, Authentication authentication) {
        if (authentication instanceof JwtAuthentication jwtAuth) {
            return ResponseEntity.ok(Map.of("inShelf", shelfService.isInShelf(jwtAuth.getUserId(), id)));
        }
        return ResponseEntity.ok(Map.of("inShelf", false));
    }

    @GetMapping("/shelf")
    public List<BookShelf> getShelf(Authentication authentication) {
        if (authentication instanceof JwtAuthentication jwtAuth) {
            return shelfService.getShelf(jwtAuth.getUserId());
        }
        return Collections.emptyList();
    }
}