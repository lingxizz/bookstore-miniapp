package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByChapterIdAndParentIdIsNullOrderByCreatedAtDesc(Integer chapterId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByChapterIdAndParentIdIsNullAndIdLessThanOrderByCreatedAtDesc(Integer chapterId, Integer cursor, Pageable pageable);
}
