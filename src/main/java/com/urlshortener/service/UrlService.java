package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.entity.Url;
import com.urlshortener.entity.User;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final Base62Encoder base62Encoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortenResponse shortenUrl(ShortenRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String shortCode = generateUniqueCode();

        LocalDateTime expiresAt = null;
        if (request.getExpiryDays() != null) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpiryDays());
        }

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .user(user)
                .expiresAt(expiresAt)
                .active(true)
                .build();

        urlRepository.save(url);
        return toResponse(url);
    }

    @Cacheable(value = "urls", key = "#shortCode")
    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            deactivateUrl(url);
            throw new UrlNotFoundException("This short URL has expired");
        }

        incrementClick(shortCode);
        return url.getOriginalUrl();
    }

    @Transactional
    public void deactivateUrl(Url url) {
        url.setActive(false);
        urlRepository.save(url);
    }

    @Transactional
    public void incrementClick(String shortCode) {
        urlRepository.incrementClickCount(shortCode);
    }

    public List<ShortenResponse> getUserUrls(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return urlRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "urls", key = "#shortCode")
    public void deleteUrl(String shortCode, String userEmail) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortCode));

        if (!url.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You don't have permission to delete this URL");
        }

        url.setActive(false);
        urlRepository.save(url);
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = base62Encoder.generate();
            attempts++;
            if (attempts > 5) throw new RuntimeException("Could not generate unique code");
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    private ShortenResponse toResponse(Url url) {
        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }
}