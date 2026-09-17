package com.activitytracking.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window rate limiter used to slow down brute-force login attempts coming
 * from a single IP address, independent of which email address is being tried.
 * <p>
 * Trade-off, on purpose: this is an in-memory, single-instance limiter. It resets
 * on restart and does not share state across multiple app instances. In a real
 * multi-instance deployment this would move to a shared store (e.g. Redis) so
 * every instance enforces the same limit. For a single-instance deployment this
 * is a correct and honest implementation of the same idea.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS_PER_WINDOW = 10;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute

    private final ConcurrentHashMap<String, Window> windowsByKey = new ConcurrentHashMap<>();

    /**
     * @return true if the request is allowed, false if the caller should be rejected (429).
     */
    public boolean tryAcquire(String key) {
        long now = Instant.now().toEpochMilli();

        Window window = windowsByKey.computeIfAbsent(key, k -> new Window(now));

        synchronized (window) {
            if (now - window.windowStartMillis > WINDOW_MILLIS) {
                // window expired, start a fresh one
                window.windowStartMillis = now;
                window.count.set(0);
            }
            return window.count.incrementAndGet() <= MAX_ATTEMPTS_PER_WINDOW;
        }
    }

    private static class Window {
        volatile long windowStartMillis;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStartMillis) {
            this.windowStartMillis = windowStartMillis;
        }
    }
}
