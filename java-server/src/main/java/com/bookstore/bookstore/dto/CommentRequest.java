package com.bookstore.bookstore.dto;

public class CommentRequest {
    private String content;
    private Integer paragraphIndex;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getParagraphIndex() { return paragraphIndex; }
    public void setParagraphIndex(Integer paragraphIndex) { this.paragraphIndex = paragraphIndex; }
}
