package com.app.hourbook.dto;

;

public record ApiResponse<T>(
        boolean success,
        String message,
        int statusCode,
        T data
       
) {
    public static <T> ApiResponse<T> success(String message,int statusCode, T data) {
        return new ApiResponse<>(
                true,
                message,
                statusCode,
                data
        );
    }
    public static <T> ApiResponse<T> error(String message,int statusCode,T data) {
        return new ApiResponse<>(
                false,
                message,
                statusCode,
                data
        );
    }
}