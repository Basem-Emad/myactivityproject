package com.activitytracking.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.activitytracking.exception.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final LoginRateLimiter rateLimiter;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public LoginRateLimitFilter(LoginRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean isLoginAttempt = "POST".equalsIgnoreCase(request.getMethod())
                && LOGIN_PATH.equals(request.getRequestURI());

        if (isLoginAttempt && !rateLimiter.tryAcquire(clientIp(request))) {
            respondTooManyRequests(response, request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Note: this reads the direct socket IP. Behind a reverse proxy / load balancer,
    // the real client IP would come from the X-Forwarded-For header instead, and
    // that header would need to be validated (only trusted proxies allowed to set it).
    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private void respondTooManyRequests(HttpServletResponse response, String path) throws IOException {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                .message("Too many login attempts from this address. Please try again in a minute.")
                .path(path)
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
