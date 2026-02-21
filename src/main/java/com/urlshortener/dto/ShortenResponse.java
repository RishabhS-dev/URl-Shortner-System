package com.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ShortenResponse {
    private String shortUrl;        // full short URL e.g. http://localhost:8080/aB3xZ9
    private String shortCode;       // just the code e.g. aB3xZ9
    private String originalUrl;
    private Long clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // null if no expiry
}