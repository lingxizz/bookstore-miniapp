package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.CommentRequest;
import com.bookstore.bookstore.entity.Comment;
import com.bookstore.bookstore.entity.ChapterSummary;
import com.bookstore.bookstore.security.JwtAuthentication;
import com.bookstore.bookstore.service.ChapterService;
import com.bookstore.bookstore.service.CommentService;
import com.bookstore.bookstore.repository.ChapterSummaryRepository;
import com.bookstore.bookstore.dto.SummaryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;
    private final ChapterService chapterService;
    private final ChapterSummaryRepository chapterSummaryRepository;

    public CommentController(CommentService commentService,
                             ChapterService chapterService,
                             ChapterSummaryRepository chapterSummaryRepository) {
        this.commentService = commentService;
        this.chapterService = chapterService;
        this.chapterSummaryRepository = chapterSummaryRepository;
    }

    @GetMapping("/chapters/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Integer id,
                                          @RequestParam(required = false) Integer cursor,
                                          @RequestParam(defaultValue = "20") Integer limit) {
        List<Comment> comments = commentService.getComments(id, cursor, limit);
        Integer nextCursor = null;
        if (comments.size() > limit) {
            Comment next = comments.remove(comments.size() - 1);
            nextCursor = next.getId();
        }
        return ResponseEntity.ok(Map.of("comments", comments, "nextCursor", nextCursor));
    }

    @PostMapping("/chapters/{id}/comments")
    public ResponseEntity<?> postComment(@PathVariable Integer id,
                                          @RequestBody CommentRequest request,
                                          Authentication authentication) {
        Integer userId = ((JwtAuthentication) authentication).getUserId();
        var chapter = chapterService.getChapter(id).orElse(null);
        if (chapter == null) {
            return ResponseEntity.ok(Map.of("error", "Chapter not found"));
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "Content required"));
        }
        Comment comment = commentService.createComment(userId, id, chapter.getBookId(), request.getContent(), request.getParagraphIndex());
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/comments/{id}/like")
    public ResponseEntity<?> likeComment(@PathVariable Integer id) {
        return commentService.likeComment(id)
                .<ResponseEntity<?>>map(c -> ResponseEntity.ok(Map.of("likes", c.getLikes())))
                .orElse(ResponseEntity.ok(Map.of("error", "Comment not found")));
    }

    @GetMapping("/chapters/{id}/summary")
    public ResponseEntity<?> getSummary(@PathVariable Integer id) {
        ChapterSummary summary = chapterSummaryRepository.findByChapterId(id).orElse(null);
        if (summary == null) {
            return ResponseEntity.ok(Map.of("content", null));
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/chapters/{id}/summary")
    public ResponseEntity<?> saveSummary(@PathVariable Integer id, @RequestBody SummaryRequest request) {
        var chapter = chapterService.getChapter(id).orElse(null);
        if (chapter == null) {
            return ResponseEntity.ok(Map.of("error", "Not found"));
        }
        ChapterSummary existing = chapterSummaryRepository.findByChapterId(id).orElse(null);
        if (existing != null) {
            existing.setContent(request.getContent());
            return ResponseEntity.ok(chapterSummaryRepository.save(existing));
        }
        ChapterSummary summary = new ChapterSummary();
        summary.setChapterId(id);
        summary.setContent(request.getContent());
        return ResponseEntity.ok(chapterSummaryRepository.save(summary));
    }
}
