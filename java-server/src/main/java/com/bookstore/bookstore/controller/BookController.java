package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Chapter;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.BookService;
import com.bookstore.bookstore.service.ChapterService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ChapterService chapterService;

    public BookController(BookService bookService, ChapterService chapterService) {
        this.bookService = bookService;
        this.chapterService = chapterService;
    }

    @GetMapping
    public List<Book> listBooks(@RequestParam(required = false) String category,
                                @RequestParam(required = false) String q) {
        return bookService.listBooks(category, q);
    }

    @GetMapping("/filter")
    public List<Book> filterBooks(@RequestParam(required = false) String category,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String sort) {
        return bookService.listBooksFiltered(category, status, sort);
    }

    @GetMapping("/{id}")
    public Book getBook(@PathVariable Integer id) {
        return bookService.getBook(id).orElse(null);
    }

    @GetMapping("/{id}/chapters")
    public List<Chapter> getChapters(@PathVariable Integer id) {
        return chapterService.getChaptersByBook(id);
    }
}