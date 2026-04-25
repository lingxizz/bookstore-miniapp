package com.bookstore.bookstore.dto;

public class AdUnlockRequest {
    private Integer chapterId;
    private String adToken;

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }
    public String getAdToken() { return adToken; }
    public void setAdToken(String adToken) { this.adToken = adToken; }
}
