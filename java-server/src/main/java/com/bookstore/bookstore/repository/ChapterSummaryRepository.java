package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.ChapterSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterSummaryRepository extends JpaRepository<ChapterSummary, Integer> {
    Optional<ChapterSummary> findByChapterId(Integer chapterId);
}
