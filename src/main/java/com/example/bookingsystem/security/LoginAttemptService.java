package com.example.bookingsystem.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory brute-force protection for the login endpoint.
 * Blocks a username after too many failed attempts within a time window.
 * (Single-instance only; use Redis for a multi-instance deployment.)
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L; // 15 minutes

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        Attempt attempt = attempts.get(key(username));
        if (attempt == null) {
            return false;
        }
        if (System.currentTimeMillis() - attempt.windowStart() > WINDOW_MS) {
            attempts.remove(key(username));
            return false;
        }
        return attempt.count() >= MAX_ATTEMPTS;
    }

    public void loginFailed(String username) {
        String k = key(username);
        attempts.compute(k, (ignored, attempt) -> {
            long now = System.currentTimeMillis();
            if (attempt == null || now - attempt.windowStart() > WINDOW_MS) {
                return new Attempt(1, now);
            }
            return new Attempt(attempt.count() + 1, attempt.windowStart());
        });
    }

    public void loginSucceeded(String username) {
        attempts.remove(key(username));
    }

    private String key(String username) {
        return username == null ? "" : username.toLowerCase();
    }

    private record Attempt(int count, long windowStart) {}
}
