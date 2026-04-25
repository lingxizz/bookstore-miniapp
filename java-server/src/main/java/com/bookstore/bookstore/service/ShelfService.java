package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.BookShelf;
import com.bookstore.bookstore.repository.BookShelfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShelfService {

    private final BookShelfRepository bookShelfRepository;

    public ShelfService(BookShelfRepository bookShelfRepository) {
        this.bookShelfRepository = bookShelfRepository;
    }

    @Transactional
    public void addToShelf(Integer userId, Integer bookId) {
        if (bookShelfRepository.findByUserIdAndBookId(userId, bookId).isPresent()) {
            return;
        }
        BookShelf shelf = new BookShelf();
        shelf.setUserId(userId);
        shelf.setBookId(bookId);
        bookShelfRepository.save(shelf);
    }

    @Transactional
    public void removeFromShelf(Integer userId, Integer bookId) {
        bookShelfRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    public boolean isInShelf(Integer userId, Integer bookId) {
        return bookShelfRepository.findByUserIdAndBookId(userId, bookId).isPresent();
    }

    @Transactional(readOnly = true)
    public List<BookShelf> getShelf(Integer userId) {
        return bookShelfRepository.findByUserIdWithBook(userId);
    }
}
