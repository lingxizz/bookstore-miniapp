package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.ProgressRequest;
import com.bookstore.bookstore.entity.ReadingRecord;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.ChapterService;
import com.bookstore.bookstore.service.ReadingRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReadingRecordController {

    private final ReadingRecordService readingRecordService;
    private final ChapterService chapterService;

    public ReadingRecordController(ReadingRecordService readingRecordService, ChapterService chapterService) {
        this.readingRecordService = readingRecordService;
        this.chapterService = chapterService;
    }

    @GetMapping("/reading-records")
    public List<ReadingRecord> getRecords(Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        return readingRecordService.getRecords(userId);
    }

    @PostMapping("/reading-records")
    public ReadingRecord saveRecord(@RequestBody ProgressRequest request, Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        return readingRecordService.saveRecord(userId, request.getChapterId(), null, request.getProgress(), request.getParagraphIndex());
    }

    @GetMapping("/books/{id}/progress")
    public ResponseEntity<?> getProgress(@PathVariable Integer id, Authentication authentication) {
        Integer userId = null;
        if (authentication instanceof JwtAuthentication jwtAuth) {
            userId = jwtAuth.getUserId();
        }
        if (userId == null) {
            java.util.Map<String, Object> empty = new java.util.HashMap<>();
            empty.put("chapterId", 0);
            empty.put("progress", 0);
            empty.put("paragraphIndex", 0);
            return ResponseEntity.ok(empty);
        }
        ReadingRecord rec = readingRecordService.getProgress(userId, id).orElse(null);
        if (rec == null) {
            java.util.Map<String, Object> empty = new java.util.HashMap<>();
            empty.put("chapterId", 0);
            empty.put("progress", 0);
            empty.put("paragraphIndex", 0);
            return ResponseEntity.ok(empty);
        }
        return ResponseEntity.ok(rec);
    }

    @PostMapping("/books/{id}/progress")
    public ResponseEntity<?> saveProgress(@PathVariable Integer id, @RequestBody ProgressRequest request, Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        ReadingRecord rec = readingRecordService.saveRecord(userId, id, request.getChapterId(), request.getProgress(), request.getParagraphIndex());
        return ResponseEntity.ok(rec);
    }

    @GetMapping("/books/{id}/read-chapters")
    public ResponseEntity<?> getReadChapters(@PathVariable Integer id, Authentication authentication) {
        Integer userId = null;
        if (authentication instanceof JwtAuthentication jwtAuth) {
            userId = jwtAuth.getUserId();
        }
        if (userId == null) {
            return ResponseEntity.ok(Map.of("chapters", List.of()));
        }
        List<Integer> chapters = chapterService.getReadChapterIds(userId, id);
        return ResponseEntity.ok(Map.of("chapters", chapters));
    }
}
