package com.bookstore.bookstore.dto;

public class SuccessResponse {
    private boolean success = true;

    public SuccessResponse() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
