package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public HomeController(BookService bookService, BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    // 首页聚合
    @GetMapping("/home")
    public Map<String, Object> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("banners", bookService.getBanners());
        data.put("categories", categoryRepository.findByOrderBySortOrderAsc());
        data.put("todayPick", bookService.getTodayPick());
        data.put("hotRank", bookService.getHotRank());
        data.put("guessLike", bookService.getGuessLike(null));
        return data;
    }

    @GetMapping("/banners")
    public List<Book> banners() {
        return bookService.getBanners();
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return categoryRepository.findByOrderBySortOrderAsc();
    }

    @GetMapping("/books/today-pick")
    public List<Book> todayPick() {
        return bookService.getTodayPick();
    }

    @GetMapping("/books/hot-rank")
    public List<Book> hotRank() {
        return bookService.getHotRank();
    }

    @GetMapping("/books/guess-like")
    public List<Book> guessLike(@RequestParam(required = false) Integer seed) {
        return bookService.getGuessLike(seed);
    }

    @GetMapping("/store")
    public Map<String, Object> store() {
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categoryRepository.findByOrderBySortOrderAsc());
        data.put("newReleases", bookService.getNewReleases());
        data.put("completed", bookService.getCompletedBooks());
        data.put("bestsellers", bookService.getHotRank());
        return data;
    }

    @GetMapping("/books/new-releases")
    public List<Book> newReleases(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return bookService.getByCategory(category);
        }
        return bookService.getNewReleases();
    }

    @GetMapping("/books/completed")
    public List<Book> completed(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return bookRepository.findByCategoryAndStatusOrderByIdDesc(category, "completed");
        }
        return bookService.getCompletedBooks();
    }
}
