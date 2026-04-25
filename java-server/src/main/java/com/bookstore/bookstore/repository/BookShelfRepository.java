package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.BookShelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookShelfRepository extends JpaRepository<BookShelf, Integer> {
    Optional<BookShelf> findByUserIdAndBookId(Integer userId, Integer bookId);
    List<BookShelf> findByUserIdOrderByCreatedAtDesc(Integer userId);
    void deleteByUserIdAndBookId(Integer userId, Integer bookId);

    @org.springframework.data.jpa.repository.Query("SELECT bs FROM BookShelf bs JOIN FETCH bs.book WHERE bs.userId = ?1 ORDER BY bs.createdAt DESC")
    List<BookShelf> findByUserIdWithBook(Integer userId);
}
