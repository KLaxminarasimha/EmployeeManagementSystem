package com.uniquehire.ems.dto;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ApiResponse<T> {
    private boolean success;
    private String  message;
    private T       data;
    private long    timestamp = System.currentTimeMillis();

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, System.currentTimeMillis());
    }
    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(true, msg, data, System.currentTimeMillis());
    }
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "Created", data, System.currentTimeMillis());
    }
}
