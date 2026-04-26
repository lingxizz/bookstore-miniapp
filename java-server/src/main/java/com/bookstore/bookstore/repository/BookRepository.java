package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Book;
import org.springframework.data.domain.Pageable;
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

    List<Book> findByRatingGreaterThanEqualOrderByRatingDesc(Double rating);

    List<Book> findByOrderByRatingDesc();

    List<Book> findByOrderByWordCountDesc();

    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category")
    List<Object[]> countByCategory();

    @Query(value = "SELECT * FROM \"Book\" ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Book> findRandom(@Param("limit") int limit);

    @Query(value = "SELECT * FROM \"Book\" OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Book> findPaginated(@Param("offset") int offset, @Param("limit") int limit);

    List<Book> findByStatusOrderByIdDesc(String status);

    List<Book> findByCategoryAndStatusOrderByIdDesc(String category, String status);

    // Sort by rating (hot) — findByOrderByRatingDesc already defined at line 23
    List<Book> findByCategoryOrderByRatingDesc(String category);
    List<Book> findByCategoryAndStatusOrderByRatingDesc(String category, String status);
    List<Book> findByStatusOrderByRatingDesc(String status);

    // Sort by rating count (popularity)
    List<Book> findByCategoryOrderByRatingCountDesc(String category);
    List<Book> findByCategoryAndStatusOrderByRatingCountDesc(String category, String status);
    List<Book> findByStatusOrderByRatingCountDesc(String status);
    List<Book> findByOrderByRatingCountDesc();

    // Sort by newest (createdAt desc)
    List<Book> findByCategoryOrderByCreatedAtDesc(String category);
    List<Book> findByCategoryAndStatusOrderByCreatedAtDesc(String category, String status);
    List<Book> findByStatusOrderByCreatedAtDesc(String status);
    List<Book> findByOrderByCreatedAtDesc();
}