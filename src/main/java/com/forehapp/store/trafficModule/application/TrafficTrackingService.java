package com.forehapp.store.trafficModule.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Records anonymous visits sent by the storefront. Fire-and-forget: anything malformed, from a bot or
 * from an internal panel is dropped silently.
 *
 * Payload: {"v": visitorId, "s": sessionId, "t": "pageview"|"ping"|"add_to_cart"|"purchase",
 *           "p": path, "r": referrer, "us"/"um"/"uc": utm source/medium/campaign, "o": orderId}
 */
@Service
public class TrafficTrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrafficTrackingService.class);
    private static final Pattern ID = Pattern.compile("^[0-9a-fA-F-]{16,36}$");
    private static final int MAX_BODY = 2048;
    /** A ping later than this after the last activity belongs to a new session; ignore it. */
    private static final int SESSION_TIMEOUT_MINUTES = 31;

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final String siteHost;

    public TrafficTrackingService(NamedParameterJdbcTemplate jdbc, ObjectMapper mapper,
                                  @Value("${app.frontend.url:}") String frontendUrl) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.siteHost = TrafficClassifier.host(frontendUrl);
    }

    public void track(String body, String userAgent) {
        if (body == null || body.length() > MAX_BODY || TrafficClassifier.isBot(userAgent)) return;
        try {
            JsonNode n = mapper.readTree(body);
            String visitor = text(n, "v", 36), session = text(n, "s", 36), type = text(n, "t", 20);
            if (visitor == null || session == null || type == null
                    || !ID.matcher(visitor).matches() || !ID.matcher(session).matches()) return;

            // Whole seconds: DATETIME columns drop fractions, and the page view check below compares to it
            LocalDateTime now = LocalDateTime.now().withNano(0);
            switch (type) {
                case "pageview" -> pageView(n, visitor, session, userAgent, now);
                case "ping" -> touch(visitor, session, now, null);
                case "add_to_cart" -> touch(visitor, session, now, "added_to_cart = 1");
                case "purchase" -> purchase(n, visitor, session, now);
                default -> { }
            }
        } catch (Exception e) {
            log.debug("[Traffic] dropped event: {}", e.getMessage());
        }
    }

    private void pageView(JsonNode n, String visitor, String session, String userAgent, LocalDateTime now) {
        String path = TrafficClassifier.cleanPath(text(n, "p", 1000));
        if (path == null || TrafficClassifier.isInternalPath(path)) return;

        String referrerHost = TrafficClassifier.host(text(n, "r", 1000));
        String utmSource = text(n, "us", 100);
        boolean product = path.startsWith("/product/");
        boolean checkout = path.startsWith("/checkout");

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("s", session).addValue("v", visitor).addValue("now", Timestamp.valueOf(now))
                .addValue("path", path)
                .addValue("source", TrafficClassifier.source(utmSource, referrerHost, siteHost))
                .addValue("refHost", referrerHost == null ? null : truncate(referrerHost, 255))
                .addValue("us", utmSource).addValue("um", text(n, "um", 100)).addValue("uc", text(n, "uc", 100))
                .addValue("device", TrafficClassifier.device(userAgent))
                .addValue("product", product ? 1 : 0).addValue("checkout", checkout ? 1 : 0);

        // First page view opens the session; later ones only add to it (same visitor, capped)
        jdbc.update("""
                INSERT INTO store_web_sessions (session_id, visitor_id, started_at, last_seen_at, page_views,
                    landing_path, source, referrer_host, utm_source, utm_medium, utm_campaign, device,
                    viewed_product, reached_checkout)
                VALUES (:s, :v, :now, :now, 1, :path, :source, :refHost, :us, :um, :uc, :device, :product, :checkout)
                ON DUPLICATE KEY UPDATE
                    last_seen_at     = IF(visitor_id = VALUES(visitor_id) AND page_views < 500, VALUES(last_seen_at), last_seen_at),
                    viewed_product   = IF(visitor_id = VALUES(visitor_id), viewed_product OR VALUES(viewed_product), viewed_product),
                    reached_checkout = IF(visitor_id = VALUES(visitor_id), reached_checkout OR VALUES(reached_checkout), reached_checkout),
                    page_views       = IF(visitor_id = VALUES(visitor_id) AND page_views < 500, page_views + 1, page_views)
                """, p);
        // Only when the session took it (its own visitor, under the cap): MySQL reports a matched row as
        // affected even when nothing changed, so check the session itself
        jdbc.update("""
                INSERT INTO store_web_page_views (session_id, path, created_at)
                SELECT :s, :path, :now FROM store_web_sessions
                WHERE session_id = :s AND visitor_id = :v AND last_seen_at = :now
                """, p);
    }

    private void touch(String visitor, String session, LocalDateTime now, String flag) {
        jdbc.update("UPDATE store_web_sessions SET last_seen_at = :now" + (flag == null ? "" : ", " + flag)
                        + " WHERE session_id = :s AND visitor_id = :v AND last_seen_at >= :since",
                new MapSqlParameterSource().addValue("s", session).addValue("v", visitor)
                        .addValue("now", Timestamp.valueOf(now))
                        .addValue("since", Timestamp.valueOf(now.minusMinutes(SESSION_TIMEOUT_MINUTES))));
    }

    private void purchase(JsonNode n, String visitor, String session, LocalDateTime now) {
        Long orderId = n.hasNonNull("o") && n.get("o").canConvertToLong() ? n.get("o").asLong() : null;
        jdbc.update("""
                UPDATE store_web_sessions SET last_seen_at = :now, purchased = 1, reached_checkout = 1,
                       order_id = COALESCE(:o, order_id)
                WHERE session_id = :s AND visitor_id = :v
                """, new MapSqlParameterSource().addValue("s", session).addValue("v", visitor)
                .addValue("now", Timestamp.valueOf(now)).addValue("o", orderId));
    }

    private static String text(JsonNode n, String field, int max) {
        if (!n.hasNonNull(field) || !n.get(field).isTextual()) return null;
        String s = n.get(field).asText().trim();
        return s.isEmpty() ? null : truncate(s, max);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
