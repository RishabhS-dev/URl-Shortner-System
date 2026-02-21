package com.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Uniform wrapper for ALL API responses.
 * Frontend always gets: { success, message, data }
 * Makes error handling consistent and predictable.
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // Success with data
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // Success without data
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // Error
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}