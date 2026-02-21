package com.urlshortener.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * BASE62 ENCODER — The heart of this system
 *
 * WHAT IS BASE62?
 * Uses characters: 0-9 (10) + a-z (26) + A-Z (26) = 62 total characters
 *
 * WHY BASE62 AND NOT OTHER OPTIONS?
 * - Base64: has +, /, = which break URLs
 * - UUID: too long (36 chars), ugly
 * - Numeric IDs: predictable, enumerable (security risk)
 * - Base62: URL-safe, short, human-readable ✅
 *
 * HOW MANY URLS CAN WE SUPPORT?
 * 6 characters: 62^6 = 56,800,235,584 (~56 billion) unique codes
 * At 1 million URLs/day → lasts 155 years
 *
 * HOW IT WORKS (explain in interviews):
 * 1. Generate a random 64-bit number using SecureRandom
 * 2. Repeatedly take (number % 62) as index into our charset
 * 3. Divide by 62, repeat until we have 6 characters
 * 4. Check DB for collision → retry if collision found (very rare)
 *
 * TIME COMPLEXITY:
 * - generateCode(): O(1) — fixed 6 iterations always
 * - DB lookup for collision: O(log n) — B-tree index
 * - Average retries: essentially 0 (collision probability = 1/56 billion)
 */
@Component
public class Base62Encoder {

    private static final String CHARSET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;
    private static final int CODE_LENGTH = 6;

    // SecureRandom is cryptographically strong — much better than Math.random()
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a random 6-character Base62 code.
     * Example output: "aB3xZ9", "q7Kp2M"
     */
    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        long num = Math.abs(SECURE_RANDOM.nextLong());

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARSET.charAt((int) (num % BASE)));
            num /= BASE;
        }

        return code.toString();
    }
}