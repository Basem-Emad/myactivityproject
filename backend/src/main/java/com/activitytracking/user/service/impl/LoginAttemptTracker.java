package com.activitytracking.user.service.impl;

import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Persists failed-login bookkeeping in its own, independent transaction.
 * <p>
 * This must run in {@link Propagation#REQUIRES_NEW}: when a login fails, the calling
 * transaction (in AuthServiceImpl) is about to be rolled back because an
 * AuthenticationException is being thrown out of it. Without a separate transaction here,
 * the failed-attempt counter we "save" would be rolled back along with everything else,
 * and the account would never actually get locked no matter how many times someone
 * fails the password.
 */
@Component
public class LoginAttemptTracker {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;

    public LoginAttemptTracker(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
        }

        userRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
}
