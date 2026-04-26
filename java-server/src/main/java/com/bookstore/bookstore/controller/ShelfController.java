package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.SuccessResponse;
import com.bookstore.bookstore.entity.BookShelf;
import com.bookstore.bookstore.entity.ReadingRecord;
import com.bookstore.bookstore.repository.ReadingRecordRepository;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.ShelfService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ShelfController {

    private final ShelfService shelfService;
    private final ReadingRecordRepository readingRecordRepository;

    public ShelfController(ShelfService shelfService, ReadingRecordRepository readingRecordRepository) {
        this.shelfService = shelfService;
        this.readingRecordRepository = readingRecordRepository;
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
    public List<Map<String, Object>> getShelf(Authentication authentication) {
        if (!(authentication instanceof JwtAuthentication jwtAuth)) {
            return Collections.emptyList();
        }
        Integer userId = jwtAuth.getUserId();
        List<BookShelf> shelves = shelfService.getShelf(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (BookShelf shelf : shelves) {
            if (shelf.getBook() == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", shelf.getId());
            item.put("bookId", shelf.getBookId());
            item.put("title", shelf.getBook().getTitle());
            item.put("author", shelf.getBook().getAuthor());
            item.put("cover", shelf.getBook().getCover());
            item.put("wordCount", shelf.getBook().getWordCount());
            item.put("status", shelf.getBook().getStatus());
            item.put("category", shelf.getBook().getCategory());
            item.put("summary", shelf.getBook().getSummary());
            item.put("rating", shelf.getBook().getRating());
            item.put("updateTime", shelf.getBook().getCreatedAt() != null
                    ? shelf.getBook().getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null);

            Optional<ReadingRecord> recordOpt = readingRecordRepository.findByUserIdAndBookId(userId, shelf.getBookId());
            double progress = recordOpt.map(ReadingRecord::getProgress).orElse(0.0);
            item.put("progress", Math.round(progress));
            item.put("readStatus", progress >= 100.0 ? "finished" : "reading");
            item.put("lastChapterId", recordOpt.map(ReadingRecord::getChapterId).orElse(null));
            item.put("lastReadAt", recordOpt.map(r -> r.getLastReadAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).orElse(null));

            result.add(item);
        }
        return result;
    }
}
