package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.ChapterRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterReadRepository extends JpaRepository<ChapterRead, Integer> {
    Optional<ChapterRead> findByUserIdAndChapterId(Integer userId, Integer chapterId);

    @Query("SELECT cr.chapterId FROM ChapterRead cr JOIN cr.chapter c WHERE cr.userId = :userId AND c.bookId = :bookId")
    List<Integer> findChapterIdsByUserIdAndBookId(@Param("userId") Integer userId, @Param("bookId") Integer bookId);
}
