package com.forehapp.store.security.filter;

import com.forehapp.store.security.jwt.JwtUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final int LOGIN_LIMIT  = 10;
    private static final int PUBLIC_LIMIT = 60;
    private static final int USER_LIMIT   = 300;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String bucketKey;
        int limit;

        if (path.equals("/api/v1/login")) {
            bucketKey = "ip:" + getClientIp(request);
            limit = LOGIN_LIMIT;
        } else {
            String userId = extractUserId(request);
            if (userId != null) {
                bucketKey = "user:" + userId;
                limit = USER_LIMIT;
            } else {
                bucketKey = "ip:" + getClientIp(request);
                limit = PUBLIC_LIMIT;
            }
        }

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> buildBucket(limit));
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
                var auth = JwtUtil.getAuthentication(bearer.replace("Bearer ", ""));
                if (auth != null) return (String) auth.getPrincipal();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
