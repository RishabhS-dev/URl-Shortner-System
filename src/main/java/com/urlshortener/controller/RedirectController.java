package com.urlshortener.controller;

import com.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * THE MOST IMPORTANT CONTROLLER
 *
 * This handles: GET /aB3xZ9  → redirect to https://original-long-url.com
 *
 * This is what users actually click. It must be:
 * - Blazing fast (cached)
 * - Public (no auth needed)
 * - Returns HTTP 302 (temporary redirect)
 *
 * WHY 302 AND NOT 301?
 * 301 = permanent (browser caches it — can't track clicks!)
 * 302 = temporary (browser always asks server — we can track every click) ✅
 */
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", originalUrl);

        // 302 Found — browser redirects to originalUrl
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}