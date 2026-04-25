package com.bookstore.bookstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ReadingRecord",
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "bookId"}))
public class ReadingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer bookId;

    private Integer chapterId;

    private Integer paragraphIndex;

    @Column(nullable = false)
    private Double progress = 0.0;

    @Column(nullable = false)
    private LocalDateTime lastReadAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        lastReadAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastReadAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }

    public Integer getParagraphIndex() { return paragraphIndex; }
    public void setParagraphIndex(Integer paragraphIndex) { this.paragraphIndex = paragraphIndex; }

    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }

    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
