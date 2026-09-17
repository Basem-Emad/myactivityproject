package com.activitytracking.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    @Test
    void tryAcquire_shouldAllowRequests_upToTheLimit() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        String key = "192.168.1.1";

        for (int i = 0; i < 10; i++) {
            assertThat(limiter.tryAcquire(key)).isTrue();
        }
    }

    @Test
    void tryAcquire_shouldRejectRequests_beyondTheLimit() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        String key = "192.168.1.2";

        for (int i = 0; i < 10; i++) {
            limiter.tryAcquire(key);
        }

        assertThat(limiter.tryAcquire(key)).isFalse();
    }

    @Test
    void tryAcquire_shouldTrackDifferentKeysIndependently() {
        LoginRateLimiter limiter = new LoginRateLimiter();

        for (int i = 0; i < 10; i++) {
            limiter.tryAcquire("192.168.1.3");
        }

        assertThat(limiter.tryAcquire("192.168.1.3")).isFalse();
        // a different IP should not be affected by the first one's usage
        assertThat(limiter.tryAcquire("192.168.1.4")).isTrue();
    }
}
