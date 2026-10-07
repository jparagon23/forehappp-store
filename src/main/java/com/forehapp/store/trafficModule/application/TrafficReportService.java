package com.forehapp.store.trafficModule.application;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.trafficModule.application.TrafficReportResponse.DayPoint;
import com.forehapp.store.trafficModule.application.TrafficReportResponse.Funnel;
import com.forehapp.store.trafficModule.application.TrafficReportResponse.Page;
import com.forehapp.store.trafficModule.application.TrafficReportResponse.Share;
import com.forehapp.store.trafficModule.application.TrafficReportResponse.Totals;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TrafficReportService {

    private static final Pattern PRODUCT_PATH = Pattern.compile("^/product/(\\d+)");

    private final NamedParameterJdbcTemplate jdbc;
    private final IStoreProfileDao storeProfileDao;

    public TrafficReportService(NamedParameterJdbcTemplate jdbc, IStoreProfileDao storeProfileDao) {
        this.jdbc = jdbc;
        this.storeProfileDao = storeProfileDao;
    }

    @Transactional(readOnly = true)
    public TrafficReportResponse report(Long userId, int days) {
        requireAdmin(userId);
        int span = Math.max(1, Math.min(days, 365));
        // Same clock the timestamps are stored with (server zone)
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(span - 1L).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        LocalDateTime prevFrom = from.minusDays(span);

        return new TrafficReportResponse(
                from.toLocalDate(), today, span,
                liveNow(),
                totals(from, to), totals(prevFrom, from),
                series(from, to), funnel(from, to), topPages(from, to), shares("source", from, to),
                shares("device", from, to));
    }

    private long liveNow() {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM store_web_sessions WHERE last_seen_at >= :since",
                new MapSqlParameterSource("since", Timestamp.valueOf(LocalDateTime.now().minusMinutes(5))), Long.class);
        return n == null ? 0 : n;
    }

    private Totals totals(LocalDateTime from, LocalDateTime to) {
        return jdbc.queryForObject("""
                SELECT COUNT(DISTINCT visitor_id) AS visitors,
                       COUNT(*) AS sessions,
                       COALESCE(SUM(page_views), 0) AS page_views,
                       COALESCE(AVG(TIMESTAMPDIFF(SECOND, started_at, last_seen_at)), 0) AS avg_duration,
                       COALESCE(AVG(page_views), 0) AS pages_per_session,
                       COALESCE(100 * SUM(page_views <= 1 AND TIMESTAMPDIFF(SECOND, started_at, last_seen_at) < 10) / COUNT(*), 0) AS bounce,
                       COALESCE(100 * SUM(purchased) / COUNT(*), 0) AS conversion,
                       (SELECT COUNT(*) FROM (SELECT visitor_id FROM store_web_sessions
                          WHERE started_at >= :from AND started_at < :to
                          GROUP BY visitor_id HAVING COUNT(*) > 1) r) AS returning_visitors
                FROM store_web_sessions
                WHERE started_at >= :from AND started_at < :to
                """, range(from, to), (rs, i) -> new Totals(
                rs.getLong("visitors"), rs.getLong("sessions"), rs.getLong("page_views"),
                scale(rs.getBigDecimal("avg_duration"), 0), scale(rs.getBigDecimal("pages_per_session"), 2),
                scale(rs.getBigDecimal("bounce"), 1), scale(rs.getBigDecimal("conversion"), 2),
                rs.getLong("returning_visitors")));
    }

    private List<DayPoint> series(LocalDateTime from, LocalDateTime to) {
        return jdbc.query("""
                SELECT DATE(started_at) AS day, COUNT(DISTINCT visitor_id) AS visitors, COUNT(*) AS sessions
                FROM store_web_sessions
                WHERE started_at >= :from AND started_at < :to
                GROUP BY DATE(started_at) ORDER BY day
                """, range(from, to),
                (rs, i) -> new DayPoint(rs.getDate("day").toLocalDate(), rs.getLong("visitors"), rs.getLong("sessions")));
    }

    private Funnel funnel(LocalDateTime from, LocalDateTime to) {
        return jdbc.queryForObject("""
                SELECT COUNT(*) AS sessions,
                       COALESCE(SUM(viewed_product OR added_to_cart OR reached_checkout OR purchased), 0) AS product,
                       COALESCE(SUM(added_to_cart OR reached_checkout OR purchased), 0) AS cart,
                       COALESCE(SUM(reached_checkout OR purchased), 0) AS checkout,
                       COALESCE(SUM(purchased), 0) AS purchased
                FROM store_web_sessions
                WHERE started_at >= :from AND started_at < :to
                """, range(from, to), (rs, i) -> new Funnel(rs.getLong("sessions"), rs.getLong("product"),
                rs.getLong("cart"), rs.getLong("checkout"), rs.getLong("purchased")));
    }

    private List<Page> topPages(LocalDateTime from, LocalDateTime to) {
        List<Page> pages = jdbc.query("""
                SELECT path, COUNT(*) AS views, COUNT(DISTINCT session_id) AS sessions
                FROM store_web_page_views
                WHERE created_at >= :from AND created_at < :to
                GROUP BY path ORDER BY views DESC LIMIT 10
                """, range(from, to),
                (rs, i) -> new Page(rs.getString("path"), null, rs.getLong("views"), rs.getLong("sessions")));

        Map<Long, String> titles = productTitles(pages);
        return pages.stream().map(p -> {
            Matcher m = PRODUCT_PATH.matcher(p.path());
            String title = m.find() ? titles.get(Long.parseLong(m.group(1))) : null;
            return new Page(p.path(), title, p.views(), p.sessions());
        }).toList();
    }

    private Map<Long, String> productTitles(List<Page> pages) {
        List<Long> ids = pages.stream().map(p -> PRODUCT_PATH.matcher(p.path()))
                .filter(Matcher::find).map(m -> Long.parseLong(m.group(1))).distinct().toList();
        Map<Long, String> titles = new HashMap<>();
        if (ids.isEmpty()) return titles;
        jdbc.query("SELECT product_id, title FROM store_products WHERE product_id IN (:ids)",
                new MapSqlParameterSource("ids", ids),
                rs -> { titles.put(rs.getLong("product_id"), rs.getString("title")); });
        return titles;
    }

    /** column is a fixed name chosen above (source / device), never user input. */
    private List<Share> shares(String column, LocalDateTime from, LocalDateTime to) {
        return jdbc.query("SELECT " + column + " AS name, COUNT(*) AS sessions, COALESCE(SUM(purchased), 0) AS purchases "
                        + "FROM store_web_sessions WHERE started_at >= :from AND started_at < :to "
                        + "GROUP BY " + column + " ORDER BY sessions DESC",
                range(from, to), (rs, i) -> new Share(rs.getString("name"), rs.getLong("sessions"), rs.getLong("purchases")));
    }

    private static MapSqlParameterSource range(LocalDateTime from, LocalDateTime to) {
        return new MapSqlParameterSource().addValue("from", Timestamp.valueOf(from)).addValue("to", Timestamp.valueOf(to));
    }

    private static BigDecimal scale(BigDecimal v, int digits) {
        return v == null ? BigDecimal.ZERO : v.setScale(digits, RoundingMode.HALF_UP);
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
    }
}
