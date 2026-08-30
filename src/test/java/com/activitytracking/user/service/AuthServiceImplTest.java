package com.activitytracking.user.service;

import com.activitytracking.security.JwtUtil;
import com.activitytracking.user.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private final String email = "admin@raya.com";

    @Test
    void authenticate_shouldReturnToken_whenCredentialsAreValid() {
        String password = "secret123";
        Authentication authentication = new TestingAuthenticationToken(
                email, password, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(email, "ADMIN")).thenReturn("mocked-jwt-token");

        String token = authService.authenticate(email, password);

        assertThat(token).isEqualTo("mocked-jwt-token");
        verify(jwtUtil, times(1)).generateToken(email, "ADMIN");
    }

    @Test
    void authenticate_shouldThrowException_whenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.authenticate(email, "wrongPassword"))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }
}