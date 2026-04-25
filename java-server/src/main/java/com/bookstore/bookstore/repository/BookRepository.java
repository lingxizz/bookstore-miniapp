package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByCategoryOrderByIdDesc(String category);

    @Query("SELECT b FROM Book b WHERE b.title LIKE %:q% OR b.author LIKE %:q% ORDER BY b.id DESC")
    List<Book> searchByTitleOrAuthor(@Param("q") String q);

    List<Book> findByOrderByIdDesc();
}
