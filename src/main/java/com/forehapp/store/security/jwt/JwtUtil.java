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

    @Value("${access.token.validity.seconds:1800}")
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

    /** Session id claim: lets a login be closed before its tokens expire. */
    public static final String SESSION_CLAIM = "sid";

    public static String createToken(String userId, java.util.Collection<? extends GrantedAuthority> authorities,
                                     String sessionId) {
        return build(userId, authorities, sessionId, "access", staticAccessValidity);
    }

    public static String createRefreshToken(String userId, java.util.Collection<? extends GrantedAuthority> authorities,
                                            String sessionId) {
        return build(userId, authorities, sessionId, "refresh", staticRefreshValidity);
    }

    public static long refreshValiditySeconds() {
        return staticRefreshValidity;
    }

    private static String build(String userId, java.util.Collection<? extends GrantedAuthority> authorities,
                                String sessionId, String type, long validitySeconds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        claims.put("type", type);
        claims.put(SESSION_CLAIM, sessionId);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validitySeconds * 1_000))
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valid access token → authentication whose details hold the session id (null for tokens issued
     * before sessions existed). Whether the session is still open is checked by the caller.
     */
    public static UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Claims claims = parse(token, "access");
        if (claims == null) return null;

        String rolesStr = claims.get("roles", String.class);
        List<SimpleGrantedAuthority> auths = rolesStr != null
                ? Arrays.stream(rolesStr.split(","))
                        .filter(s -> !s.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(claims.get("userId", String.class), null, auths);
        auth.setDetails(claims.get(SESSION_CLAIM, String.class));
        return auth;
    }

    /** Claims of a valid refresh token, or null. */
    public static Claims parseRefreshToken(String token) {
        return parse(token, "refresh");
    }

    private static Claims parse(String token, String type) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretBytes)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return type.equals(claims.get("type", String.class)) ? claims : null;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid {} token: {}", type, e.getMessage());
            return null;
        }
    }
}
