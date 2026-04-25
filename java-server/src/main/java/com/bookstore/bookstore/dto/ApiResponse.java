package com.bookstore.bookstore.dto;

public class ApiResponse<T> {
    private T data;
    private String error;

    public ApiResponse() {}

    public ApiResponse(T data) { this.data = data; }

    public ApiResponse(String error) { this.error = error; }

    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(data); }
    public static <T> ApiResponse<T> error(String error) { return new ApiResponse<>(error); }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
