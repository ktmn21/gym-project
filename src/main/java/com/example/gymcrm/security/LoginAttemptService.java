package com.example.gymcrm.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MS = 5 * 60 * 1000;

    private static class Attempt {
        int count;
        Instant blockedUntil;
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void recordFailure(String username) {
        Attempt attempt = attempts.computeIfAbsent(username, k -> new Attempt());
        attempt.count++;
        if (attempt.count >= MAX_ATTEMPTS) {
            attempt.blockedUntil = Instant.now().plusMillis(BLOCK_DURATION_MS);
        }
    }

    public void recordSuccess(String username) {
        attempts.remove(username);
    }

    public boolean isBlocked(String username) {
        Attempt attempt = attempts.get(username);
        if (attempt == null || attempt.blockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(attempt.blockedUntil)) {
            attempts.remove(username);
            return false;
        }
        return true;
    }

    public long secondsUntilUnblock(String username) {
        Attempt attempt = attempts.get(username);
        if (attempt == null || attempt.blockedUntil == null) {
            return 0;
        }
        long secs = attempt.blockedUntil.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(secs, 0);
    }
}