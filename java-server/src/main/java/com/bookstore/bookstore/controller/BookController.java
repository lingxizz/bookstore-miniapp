package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Chapter;
import com.bookstore.bookstore.repository.ChapterRepository;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.BookService;
import com.bookstore.bookstore.service.ChapterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ChapterService chapterService;
    private final ChapterRepository chapterRepository;

    public BookController(BookService bookService, ChapterService chapterService, ChapterRepository chapterRepository) {
        this.bookService = bookService;
        this.chapterService = chapterService;
        this.chapterRepository = chapterRepository;
    }

    private void enrichChapterCount(List<Book> books) {
        for (Book book : books) {
            if (book.getId() != null) {
                book.setChapterCount((int) chapterRepository.countByBookId(book.getId()));
            }
        }
    }

    @GetMapping
    public List<Book> listBooks(@RequestParam(required = false) String category,
                                @RequestParam(required = false) String q) {
        List<Book> books = bookService.listBooks(category, q);
        enrichChapterCount(books);
        return books;
    }

    @GetMapping("/filter")
    public List<Book> filterBooks(@RequestParam(required = false) String category,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String sort) {
        List<Book> books = bookService.listBooksFiltered(category, status, sort);
        enrichChapterCount(books);
        return books;
    }

    @GetMapping("/{id}")
    public Book getBook(@PathVariable Integer id) {
        Book book = bookService.getBook(id).orElse(null);
        if (book != null && book.getId() != null) {
            book.setChapterCount((int) chapterRepository.countByBookId(book.getId()));
        }
        return book;
    }

    @GetMapping("/{id}/chapters")
    public List<Chapter> getChapters(@PathVariable Integer id) {
        return chapterService.getChaptersByBook(id);
    }

    @GetMapping("/{bookId}/chapters/{chapterId}")
    public ResponseEntity<?> getChapterContent(
            @PathVariable Integer bookId,
            @PathVariable Integer chapterId,
            Authentication authentication) {
        Chapter chapter = chapterService.getChapter(chapterId).orElse(null);
        if (chapter == null || !chapter.getBookId().equals(bookId)) {
            return ResponseEntity.ok(Map.of("error", "Not found"));
        }

        boolean isFree = chapter.getIsFree();
        if (!isFree) {
            Integer userId = getUserId(authentication);
            if (userId == null || !chapterService.isUnlocked(userId, chapterId)) {
                Map<String, Object> lockedResponse = new HashMap<>();
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

    private Integer getUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthentication jwtAuth) {
            return jwtAuth.getUserId();
        }
        return null;
    }
}
