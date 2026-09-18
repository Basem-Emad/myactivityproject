package com.activitytracking.user.service.impl;

import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptTrackerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginAttemptTracker tracker;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void registerFailedAttempt_shouldIncrementCounter_withoutLocking_belowThreshold() {
        tracker.registerFailedAttempt(1L);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void registerFailedAttempt_shouldLockAccount_onFifthFailure() {
        user.setFailedLoginAttempts(4);

        tracker.registerFailedAttempt(1L);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getLockedUntil()).isNotNull().isAfter(Instant.now());
    }

    @Test
    void resetFailedAttempts_shouldClearCounterAndLock() {
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(Instant.now().plusSeconds(600));

        tracker.resetFailedAttempts(1L);

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository, times(1)).save(user);
    }
}
