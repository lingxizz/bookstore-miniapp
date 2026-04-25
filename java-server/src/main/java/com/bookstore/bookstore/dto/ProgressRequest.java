package com.bookstore.bookstore.dto;

public class ProgressRequest {
    private Integer chapterId;
    private Double progress;

    private Integer paragraphIndex;

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }
    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }

    public Integer getParagraphIndex() { return paragraphIndex; }
    public void setParagraphIndex(Integer paragraphIndex) { this.paragraphIndex = paragraphIndex; }
}
