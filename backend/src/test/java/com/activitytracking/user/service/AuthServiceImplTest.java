package com.activitytracking.user.service;

import com.activitytracking.security.JwtUtil;
import com.activitytracking.user.dto.response.AuthResponseDto;
import com.activitytracking.user.entity.RefreshToken;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.RefreshTokenRepository;
import com.activitytracking.user.repository.UserRepository;
import com.activitytracking.user.service.impl.AuthServiceImpl;
import com.activitytracking.user.service.impl.LoginAttemptTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginAttemptTracker loginAttemptTracker;

    private AuthServiceImpl authService;

    private final String email = "admin@raya.com";
    private final String password = "secret123";
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager, userRepository, refreshTokenRepository, jwtUtil, loginAttemptTracker);
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604_800_000L);

        Role adminRole = Role.builder().id(1L).name("ADMIN").permissions(Set.of()).build();

        user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRole(adminRole);
        user.setActive(true);
        user.setLockedUntil(null);
    }

    @Test
    void login_shouldReturnTokensAndResetAttempts_whenCredentialsAreValid() {
        Authentication authentication = new TestingAuthenticationToken(email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(email, "ADMIN")).thenReturn("mocked-access-token");

        AuthResponseDto response = authService.login(email, password);

        assertThat(response.getAccessToken()).isEqualTo("mocked-access-token");
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getRole()).isEqualTo("ADMIN");
        verify(loginAttemptTracker, times(1)).resetFailedAttempts(1L);
        verify(loginAttemptTracker, never()).registerFailedAttempt(any());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void login_shouldRegisterFailedAttempt_whenPasswordIsWrong() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(email, "wrongPassword"))
                .isInstanceOf(BadCredentialsException.class);

        verify(loginAttemptTracker, times(1)).registerFailedAttempt(1L);
        verify(loginAttemptTracker, never()).resetFailedAttempts(any());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void login_shouldThrowLockedException_whenAccountIsCurrentlyLocked() {
        user.setLockedUntil(Instant.now().plusSeconds(600));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(LockedException.class);

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(loginAttemptTracker);
    }

    @Test
    void login_shouldProceed_whenPreviousLockHasExpired() {
        user.setLockedUntil(Instant.now().minusSeconds(60)); // lock window already passed

        Authentication authentication = new TestingAuthenticationToken(email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(email, "ADMIN")).thenReturn("mocked-access-token");

        AuthResponseDto response = authService.login(email, password);

        assertThat(response.getAccessToken()).isEqualTo("mocked-access-token");
    }

    @Test
    void refresh_shouldRotateToken_whenRefreshTokenIsValid() {
        RefreshToken stored = RefreshToken.builder()
                .id(1L)
                .token("old-refresh-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(stored));
        when(jwtUtil.generateToken(email, "ADMIN")).thenReturn("new-access-token");

        AuthResponseDto response = authService.refresh("old-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNotEqualTo("old-refresh-token");
        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // revoke old + save new
    }

    @Test
    void refresh_shouldThrowException_whenTokenIsUnknown() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_shouldThrowException_whenTokenIsExpired() {
        RefreshToken expired = RefreshToken.builder()
                .id(1L)
                .token("expired-token")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(60))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_shouldThrowException_whenTokenIsAlreadyRevoked() {
        RefreshToken revoked = RefreshToken.builder()
                .id(1L)
                .token("revoked-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh("revoked-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void logout_shouldRevokeToken_whenTokenExists() {
        RefreshToken stored = RefreshToken.builder()
                .id(1L)
                .token("some-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("some-token")).thenReturn(Optional.of(stored));

        authService.logout("some-token");

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(stored);
    }

    @Test
    void logout_shouldNotThrow_whenTokenIsUnknown() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        authService.logout("unknown");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}
