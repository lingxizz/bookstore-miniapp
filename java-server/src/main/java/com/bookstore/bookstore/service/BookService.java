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

    public List<Book> listBooksFiltered(String category, String status, String sort) {
        boolean hasCat = category != null && !category.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();
        String sortKey = (sort == null || sort.isEmpty()) ? "hot" : sort;

        return switch (sortKey) {
            case "rating" -> hasCat && hasStatus ? bookRepository.findByCategoryAndStatusOrderByRatingDesc(category, status)
                    : hasCat ? bookRepository.findByCategoryOrderByRatingDesc(category)
                    : hasStatus ? bookRepository.findByStatusOrderByRatingDesc(status)
                    : bookRepository.findByOrderByRatingDesc();
            case "new" -> hasCat && hasStatus ? bookRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, status)
                    : hasCat ? bookRepository.findByCategoryOrderByCreatedAtDesc(category)
                    : hasStatus ? bookRepository.findByStatusOrderByCreatedAtDesc(status)
                    : bookRepository.findByOrderByCreatedAtDesc();
            default /* hot */ -> hasCat && hasStatus ? bookRepository.findByCategoryAndStatusOrderByRatingCountDesc(category, status)
                    : hasCat ? bookRepository.findByCategoryOrderByRatingCountDesc(category)
                    : hasStatus ? bookRepository.findByStatusOrderByRatingCountDesc(status)
                    : bookRepository.findByOrderByRatingCountDesc();
        };
    }

    public Optional<Book> getBook(Integer id) {
        return bookRepository.findById(id);
    }

    public List<Book> getBanners() {
        return bookRepository.findByRatingGreaterThanEqualOrderByRatingDesc(9.0);
    }

    public List<Book> getHotRank() {
        return bookRepository.findByOrderByRatingDesc();
    }

    public List<Book> getGuessLike(Integer seed) {
        if (seed == null) {
            return bookRepository.findByOrderByWordCountDesc();
        }
        long count = bookRepository.count();
        int limit = Math.toIntExact(Math.min(count, 9));
        int offset = Math.max(0, (seed * 7) % Math.max(1, (int) count - limit));
        return bookRepository.findPaginated(offset, limit);
    }

    public List<Book> getTodayPick() {
        List<Book> top = bookRepository.findByRatingGreaterThanEqualOrderByRatingDesc(9.0);
        return top.isEmpty() ? List.of() : List.of(top.get(0));
    }

    public List<Object[]> getCategoryCounts() {
        return bookRepository.countByCategory();
    }

    public List<Book> getNewReleases() {
        return bookRepository.findByOrderByIdDesc();
    }

    public List<Book> getCompletedBooks() {
        return bookRepository.findByStatusOrderByIdDesc("completed");
    }

    public List<Book> getByCategory(String category) {
        return bookRepository.findByCategoryOrderByIdDesc(category);
    }
}