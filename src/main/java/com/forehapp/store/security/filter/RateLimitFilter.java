package com.forehapp.store.security.filter;

import com.forehapp.store.security.jwt.JwtUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

/**
 * Requests per minute: account endpoints (login, codes, email checks) per IP, the rest per user or IP.
 * Buckets expire when idle and the table is capped, so made-up clients cannot fill the memory.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int AUTH_LIMIT   = 10;
    private static final int PUBLIC_LIMIT = 60;
    private static final int USER_LIMIT   = 300;

    /** Endpoints that check a secret or reveal accounts: tight limit per IP. */
    private static final Set<String> AUTH_PATHS = Set.of(
            "/api/v1/login",
            "/api/v1/auth/verify-code",
            "/api/v1/auth/resend-code",
            "/api/v1/auth/check-email",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/checkout/guest/create-account");

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    /**
     * Proxies in front of the app that append to X-Forwarded-For (Railway: 1). The client IP is the
     * entry that many places from the end; anything before it was written by the client and is ignored.
     */
    private final int trustedProxyHops;

    public RateLimitFilter(@Value("${app.security.trusted-proxy-hops:1}") int trustedProxyHops) {
        this.trustedProxyHops = Math.max(0, trustedProxyHops);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String bucketKey;
        int limit;

        if (AUTH_PATHS.contains(path)) {
            bucketKey = "auth:" + clientIp(request);
            limit = AUTH_LIMIT;
        } else {
            String userId = extractUserId(request);
            if (userId != null) {
                bucketKey = "user:" + userId;
                limit = USER_LIMIT;
            } else {
                bucketKey = "ip:" + clientIp(request);
                limit = PUBLIC_LIMIT;
            }
        }

        Bucket bucket = buckets.get(bucketKey, k -> buildBucket(limit));
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
        }
    }

    private Bucket buildBucket(int limit) {
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private String extractUserId(HttpServletRequest request) {
        try {
            String bearer = request.getHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) {
                var auth = JwtUtil.getAuthentication(bearer.substring(7));
                if (auth != null) return (String) auth.getPrincipal();
            }
        } catch (Exception ignored) {}
        return null;
    }

    String clientIp(HttpServletRequest request) {
        return clientIp(request.getHeader("X-Forwarded-For"), request.getRemoteAddr(), trustedProxyHops);
    }

    static String clientIp(String forwardedFor, String remoteAddr, int hops) {
        if (hops == 0 || forwardedFor == null || forwardedFor.isBlank()) return remoteAddr;
        String[] parts = forwardedFor.split(",");
        int index = parts.length - hops;
        String ip = index >= 0 ? parts[index].trim() : parts[0].trim();
        return ip.isEmpty() ? remoteAddr : ip;
    }
}
