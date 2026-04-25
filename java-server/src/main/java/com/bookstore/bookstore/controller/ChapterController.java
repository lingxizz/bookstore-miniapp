package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.SuccessResponse;
import com.bookstore.bookstore.entity.Chapter;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.ChapterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChapter(@PathVariable Integer id, Authentication authentication) {
        Chapter chapter = chapterService.getChapter(id).orElse(null);
        if (chapter == null) {
            return ResponseEntity.ok(Map.of("error", "Not found"));
        }
        boolean isFree = chapter.getIsFree();
        if (!isFree) {
            Integer userId = getUserId(authentication);
            if (userId == null || !chapterService.isUnlocked(userId, id)) {
                java.util.Map<String, Object> lockedResponse = new java.util.HashMap<>();
                lockedResponse.put("id", chapter.getId());
                lockedResponse.put("bookId", chapter.getBookId());
                lockedResponse.put("title", chapter.getTitle());
                lockedResponse.put("order", chapter.getOrder());
                lockedResponse.put("price", chapter.getPrice());
                lockedResponse.put("isFree", chapter.getIsFree());
                lockedResponse.put("createdAt", chapter.getCreatedAt());
                lockedResponse.put("content", "");
                lockedResponse.put("locked", true);
                return ResponseEntity.ok(lockedResponse);
            }
        }
        return ResponseEntity.ok(chapter);
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<?> unlockChapter(@PathVariable Integer id, Authentication authentication) {
        Integer userId = getUserId(authentication);
        if (userId == null) {
            return ResponseEntity.ok(Map.of("error", "Unauthorized"));
        }
        boolean success = chapterService.unlockChapter(userId, id);
        if (!success) {
            return ResponseEntity.ok(Map.of("error", "Insufficient coins"));
        }
        return ResponseEntity.ok(new SuccessResponse());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Integer id, Authentication authentication) {
        Integer userId = getUserId(authentication);
        if (userId == null) {
            return ResponseEntity.ok(Map.of("error", "Unauthorized"));
        }
        chapterService.markChapterRead(userId, id);
        return ResponseEntity.ok(new SuccessResponse());
    }

    private Integer getUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthentication jwtAuth) {
            return jwtAuth.getUserId();
        }
        return null;
    }
}
