package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.UnlockedChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnlockedChapterRepository extends JpaRepository<UnlockedChapter, Integer> {
    Optional<UnlockedChapter> findByUserIdAndChapterId(Integer userId, Integer chapterId);
}
