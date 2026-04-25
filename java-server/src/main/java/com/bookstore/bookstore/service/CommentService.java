package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Comment;
import com.bookstore.bookstore.repository.CommentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getComments(Integer chapterId, Integer cursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        if (cursor != null && cursor > 0) {
            return commentRepository.findByChapterIdAndParentIdIsNullAndIdLessThanOrderByCreatedAtDesc(chapterId, cursor, pageable);
        }
        return commentRepository.findByChapterIdAndParentIdIsNullOrderByCreatedAtDesc(chapterId, pageable);
    }

    public Comment createComment(Integer userId, Integer chapterId, Integer bookId, String content, Integer paragraphIndex) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setBookId(bookId);
        comment.setChapterId(chapterId);
        comment.setContent(content.trim());
        comment.setParagraphIndex(paragraphIndex);
        return commentRepository.save(comment);
    }

    public Optional<Comment> likeComment(Integer commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return Optional.empty();
        comment.setLikes(comment.getLikes() + 1);
        return Optional.of(commentRepository.save(comment));
    }
}
