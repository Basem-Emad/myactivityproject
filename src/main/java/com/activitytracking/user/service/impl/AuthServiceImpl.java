package com.activitytracking.user.service.impl;

import com.activitytracking.security.JwtUtil;
import com.activitytracking.user.dto.response.AuthResponseDto;
import com.activitytracking.user.entity.RefreshToken;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.RefreshTokenRepository;
import com.activitytracking.user.repository.UserRepository;
import com.activitytracking.user.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final LoginAttemptTracker loginAttemptTracker;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                            UserRepository userRepository,
                            RefreshTokenRepository refreshTokenRepository,
                            JwtUtil jwtUtil,
                            LoginAttemptTracker loginAttemptTracker) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.loginAttemptTracker = loginAttemptTracker;
    }

    @Override
    public AuthResponseDto login(String email, String password) {
        // Same generic message whether the email doesn't exist or the password is wrong,
        // so a caller can't use this endpoint to enumerate which emails are registered.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new LockedException("Account is temporarily locked due to too many failed attempts. "
                    + "Please try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException ex) {
            // Runs in its own transaction (see LoginAttemptTracker) so the counter
            // survives even though this method's transaction is about to roll back.
            loginAttemptTracker.registerFailedAttempt(user.getId());
            throw ex;
        }

        loginAttemptTracker.resetFailedAttempts(user.getId());

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
        String refreshToken = issueRefreshToken(user);

        return new AuthResponseDto(accessToken, refreshToken, user.getEmail(), user.getRole().getName());
    }

    @Override
    public AuthResponseDto refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiryDate().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired or revoked, please log in again");
        }

        User user = stored.getUser();

        // Rotate on every use: the old refresh token is burned so it can't be replayed,
        // and a brand new one is handed back alongside the new access token.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
        String newRefreshToken = issueRefreshToken(user);

        return new AuthResponseDto(accessToken, newRefreshToken, user.getEmail(), user.getRole().getName());
    }

    @Override
    public void logout(String refreshTokenValue) {
        // Idempotent on purpose: logging out with an already-invalid or unknown token
        // is not an error, the end state the caller wants (being logged out) is already true.
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private String issueRefreshToken(User user) {
        // Opaque random value, deliberately NOT a JWT: it carries no data of its own,
        // it is only a lookup key into the refresh_tokens table, which is what lets us
        // revoke it on demand. Two concatenated UUIDs give ~244 bits of randomness.
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
