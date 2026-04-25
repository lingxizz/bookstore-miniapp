package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Chapter;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.ChapterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;

    public AdminController(BookRepository bookRepository, ChapterRepository chapterRepository) {
        this.bookRepository = bookRepository;
        this.chapterRepository = chapterRepository;
    }

    @PostMapping("/books/import")
    public ResponseEntity<?> importBooks(@RequestBody List<Map<String, Object>> books) {
        int bookCount = 0;
        int chapterCount = 0;

        for (Map<String, Object> bookData : books) {
            Book book = new Book();
            book.setTitle(getString(bookData, "title"));
            book.setAuthor(getString(bookData, "author"));
            book.setCover(getString(bookData, "cover"));
            book.setSummary(getString(bookData, "summary"));
            book.setCategory(getString(bookData, "category"));
            book.setTags(getString(bookData, "tags"));
            book.setPrice(getInt(bookData, "price", 10));
            book.setStatus(getString(bookData, "status", "finished"));
            book.setWordCount(getInt(bookData, "wordCount", 0));
            book.setRating(getDouble(bookData, "rating", 8.0));
            book.setRatingCount(getInt(bookData, "ratingCount", 0));

            Book saved = bookRepository.save(book);
            bookCount++;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> chapters = (List<Map<String, Object>>) bookData.get("chapters");
            if (chapters != null) {
                int freeCount = getInt(bookData, "freeChapters", 10);
                for (int i = 0; i < chapters.size(); i++) {
                    Map<String, Object> chData = chapters.get(i);
                    Chapter chapter = new Chapter();
                    chapter.setBookId(saved.getId());
                    chapter.setTitle(getString(chData, "title"));
                    chapter.setOrder(getInt(chData, "order", i + 1));
                    chapter.setContent(getString(chData, "content", ""));
                    chapter.setIsFree(i < freeCount);
                    chapter.setPrice(getInt(chData, "price", 0));
                    chapterRepository.save(chapter);
                    chapterCount++;
                }
            }
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "books", bookCount,
            "chapters", chapterCount
        ));
    }

    private String getString(Map<String, Object> map, String key) {
        return getString(map, key, null);
    }

    private String getString(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }

    private int getInt(Map<String, Object> map, String key, int defaultVal) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        }
        return defaultVal;
    }

    private double getDouble(Map<String, Object> map, String key, double defaultVal) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        }
        return defaultVal;
    }
}
