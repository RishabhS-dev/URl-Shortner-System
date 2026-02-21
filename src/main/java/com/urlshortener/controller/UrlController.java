package com.urlshortener.controller;

import com.urlshortener.dto.ApiResponse;
import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    /**
     * POST /api/urls/shorten
     * Header: Authorization: Bearer <token>
     * Body: { "originalUrl": "https://example.com", "expiryDays": 30 }
     */
    @PostMapping("/shorten")
    public ResponseEntity<ApiResponse<ShortenResponse>> shortenUrl(
            @Valid @RequestBody ShortenRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ShortenResponse response = urlService.shortenUrl(request, userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Short URL created!", response));
    }

    /**
     * GET /api/urls/my
     * Header: Authorization: Bearer <token>
     * Returns all URLs created by the logged-in user
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ShortenResponse>>> getMyUrls(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ShortenResponse> urls = urlService.getUserUrls(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("URLs fetched successfully", urls));
    }

    /**
     * DELETE /api/urls/{shortCode}
     * Header: Authorization: Bearer <token>
     * Soft-deletes the URL (sets active=false)
     */
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {

        urlService.deleteUrl(shortCode, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("URL deleted successfully"));
    }
}