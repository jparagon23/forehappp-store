package com.forehapp.store.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${store.jwt.secret}")
    private String jwtSecret;

    @Value("${access.token.validity.seconds:86400}")
    private Long accessTokenValiditySeconds;

    @Value("${refresh.token.validity.seconds:604800}")
    private Long refreshTokenValiditySeconds;

    private static byte[] secretBytes;
    private static Long staticAccessValidity;
    private static Long staticRefreshValidity;

    @PostConstruct
    public void init() {
        secretBytes = jwtSecret.getBytes();
        staticAccessValidity = accessTokenValiditySeconds;
        staticRefreshValidity = refreshTokenValiditySeconds;
    }

    public static String createToken(String userId, java.util.Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        claims.put("type", "access");
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + staticAccessValidity * 1_000))
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    public static String createRefreshToken(String userId, java.util.Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        claims.put("type", "refresh");
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + staticRefreshValidity * 1_000))
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    public static UsernamePasswordAuthenticationToken getAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretBytes)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (!"access".equals(claims.get("type", String.class))) return null;

            String userId = claims.get("userId", String.class);
            String rolesStr = claims.get("roles", String.class);
            List<SimpleGrantedAuthority> auths = rolesStr != null
                    ? Arrays.stream(rolesStr.split(","))
                            .filter(s -> !s.isBlank())
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList())
                    : Collections.emptyList();

            return new UsernamePasswordAuthenticationToken(userId, null, auths);
        } catch (JwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    public static Map<String, String> refreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretBytes)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            if (!"refresh".equals(claims.get("type", String.class))) return null;

            String userId = claims.get("userId", String.class);
            String rolesStr = claims.get("roles", String.class);
            List<SimpleGrantedAuthority> auths = rolesStr != null
                    ? Arrays.stream(rolesStr.split(","))
                            .filter(s -> !s.isBlank())
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList())
                    : Collections.emptyList();

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", createToken(userId, auths));
            tokens.put("refresh_token", createRefreshToken(userId, auths));
            return tokens;
        } catch (JwtException e) {
            logger.warn("Invalid refresh token: {}", e.getMessage());
            return null;
        }
    }
}
