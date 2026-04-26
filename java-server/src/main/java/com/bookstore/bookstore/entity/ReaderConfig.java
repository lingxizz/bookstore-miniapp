package com.bookstore.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ReaderConfig")
public class ReaderConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer userId;

    @Column(nullable = false)
    private Integer fontSize = 18;

    @Column(nullable = false)
    private Integer lineHeight = 160;

    @Column(nullable = false)
    private String theme = "light";

    @Column(nullable = false)
    private Integer brightness = 100;

    @Column(nullable = false)
    private Integer paragraphSpacing = 24;

    @Column(nullable = false)
    private String fontFamily = "serif";

    @Column(nullable = false)
    private String pagingMode = "scroll";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }

    public Integer getLineHeight() { return lineHeight; }
    public void setLineHeight(Integer lineHeight) { this.lineHeight = lineHeight; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public Integer getBrightness() { return brightness; }
    public void setBrightness(Integer brightness) { this.brightness = brightness; }

    public Integer getParagraphSpacing() { return paragraphSpacing; }
    public void setParagraphSpacing(Integer paragraphSpacing) { this.paragraphSpacing = paragraphSpacing; }

    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }

    public String getPagingMode() { return pagingMode; }
    public void setPagingMode(String pagingMode) { this.pagingMode = pagingMode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
