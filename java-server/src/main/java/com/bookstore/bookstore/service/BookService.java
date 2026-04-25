package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> listBooks(String category, String q) {
        if (q != null && !q.isEmpty()) {
            return bookRepository.searchByTitleOrAuthor(q);
        }
        if (category != null && !category.isEmpty()) {
            return bookRepository.findByCategoryOrderByIdDesc(category);
        }
        return bookRepository.findByOrderByIdDesc();
    }

    public Optional<Book> getBook(Integer id) {
        return bookRepository.findById(id);
    }
}
