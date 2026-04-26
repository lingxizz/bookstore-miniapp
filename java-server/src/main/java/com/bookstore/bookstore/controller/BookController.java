package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Chapter;
import com.bookstore.bookstore.repository.ChapterRepository;
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
}
