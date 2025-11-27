package com.coupang.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight rate limiter that watches well-known scanner paths (.git, wp-admin, etc.)
 * and temporarily blocks IPs that repeatedly request them.
 */
@Component
public class SuspiciousPathFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SuspiciousPathFilter.class);

    private static final Set<String> SUSPICIOUS_KEYWORDS = Set.of(
            ".git",
            "wp-admin",
            "wp-content",
            "wp-login",
            "wordpress",
            "/config.",
            "/phpmyadmin",
            "/shell",
            "/vendor/",
            "/env"
    );

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = Duration.ofMinutes(10).toMillis();
    private static final long BLOCK_DURATION_MILLIS = Duration.ofMinutes(30).toMillis();

    private final Map<String, Deque<Long>> attemptLog = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedIps = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI().toLowerCase(Locale.ROOT);

        if (!isSuspiciousPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);

        if (isBlocked(clientIp)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden");
            return;
        }

        if (incrementAndCheckLimit(clientIp)) {
            blockedIps.put(clientIp, System.currentTimeMillis());
            log.warn("Blocking IP {} for hitting suspicious path {} too often", clientIp, path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSuspiciousPath(String path) {
        return SUSPICIOUS_KEYWORDS.stream().anyMatch(path::contains);
    }

    private boolean incrementAndCheckLimit(String ip) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = attemptLog.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.removeFirst();
            }
            timestamps.addLast(now);
            return timestamps.size() >= MAX_ATTEMPTS;
        }
    }

    private boolean isBlocked(String ip) {
        Long blockedAt = blockedIps.get(ip);
        if (blockedAt == null) {
            return false;
        }
        if (System.currentTimeMillis() - blockedAt > BLOCK_DURATION_MILLIS) {
            blockedIps.remove(ip);
            attemptLog.remove(ip);
            return false;
        }
        return true;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}


