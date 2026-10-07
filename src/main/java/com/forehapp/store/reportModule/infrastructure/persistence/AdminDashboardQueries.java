package com.forehapp.store.reportModule.infrastructure.persistence;

import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.Catalog;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.DayPoint;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.Operations;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.RecentOrder;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.Reviews;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.SupplierSync;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.TopProduct;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/** Read-only aggregates for the admin dashboard. */
@Repository
public class AdminDashboardQueries {

    /** An order counts as sold: not cancelled, and paid or cash on delivery. */
    private static final String SOLD = """
            o.status <> 'CANCELLED'
            AND (o.status IN ('PAID', 'PAYMENT_CONFIRMED') OR o.payment_method = 'CASH_ON_DELIVERY')
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public AdminDashboardQueries(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record SalesTotals(long orders, BigDecimal revenue, long buyers) {}

    public record ProfitTotals(BigDecimal profit, long items, long itemsWithoutCost) {}

    public SalesTotals sales(LocalDateTime from, LocalDateTime to) {
        return jdbc.queryForObject("""
                SELECT COUNT(*) AS orders, COALESCE(SUM(o.total), 0) AS revenue,
                       COUNT(DISTINCT COALESCE(LOWER(o.buyer_email), CONCAT('profile:', o.buyer_id))) AS buyers
                FROM store_orders o
                WHERE o.created_at >= :from AND o.created_at < :to AND
                """ + SOLD, range(from, to),
                (rs, i) -> new SalesTotals(rs.getLong("orders"), rs.getBigDecimal("revenue"), rs.getLong("buyers")));
    }

    public ProfitTotals profit(LocalDateTime from, LocalDateTime to) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(CASE WHEN i.unit_cost IS NOT NULL
                                         THEN (i.unit_price - i.unit_cost) * i.quantity END), 0) AS profit,
                       COUNT(*) AS items,
                       COALESCE(SUM(CASE WHEN i.unit_cost IS NULL THEN 1 ELSE 0 END), 0) AS without_cost
                FROM store_order_items i
                JOIN store_order_seller_groups g ON g.group_id = i.group_id
                JOIN store_orders o ON o.order_id = g.order_id
                WHERE g.status <> 'CANCELLED' AND o.created_at >= :from AND o.created_at < :to AND
                """ + SOLD, range(from, to),
                (rs, i) -> new ProfitTotals(rs.getBigDecimal("profit"), rs.getLong("items"), rs.getLong("without_cost")));
    }

    public long newUsers(LocalDateTime from, LocalDateTime to) {
        Long n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE creation_date >= :from AND creation_date < :to",
                range(from, to), Long.class);
        return n == null ? 0 : n;
    }

    public List<DayPoint> series(LocalDateTime from, LocalDateTime to) {
        return jdbc.query("""
                SELECT DATE(o.created_at) AS day, COUNT(*) AS orders, COALESCE(SUM(o.total), 0) AS revenue
                FROM store_orders o
                WHERE o.created_at >= :from AND o.created_at < :to AND
                """ + SOLD + " GROUP BY DATE(o.created_at) ORDER BY day", range(from, to),
                (rs, i) -> new DayPoint(rs.getDate("day").toLocalDate(), rs.getLong("orders"), rs.getBigDecimal("revenue")));
    }

    public List<TopProduct> topProducts(LocalDateTime from, LocalDateTime to, int limit) {
        return jdbc.query("""
                SELECT p.product_id, p.title, SUM(i.quantity) AS units, SUM(i.unit_price * i.quantity) AS revenue
                FROM store_order_items i
                JOIN store_order_seller_groups g ON g.group_id = i.group_id
                JOIN store_orders o ON o.order_id = g.order_id
                JOIN store_product_variants v ON v.variant_id = i.variant_id
                JOIN store_products p ON p.product_id = v.product_id
                WHERE g.status <> 'CANCELLED' AND o.created_at >= :from AND o.created_at < :to AND
                """ + SOLD + """
                 GROUP BY p.product_id, p.title
                ORDER BY units DESC, revenue DESC
                LIMIT :limit
                """, range(from, to).addValue("limit", limit),
                (rs, i) -> new TopProduct(rs.getLong("product_id"), rs.getString("title"),
                        rs.getLong("units"), rs.getBigDecimal("revenue")));
    }

    public Operations operations() {
        return jdbc.queryForObject("""
                SELECT
                  (SELECT COUNT(*) FROM store_orders o
                     WHERE o.status = 'PENDING' AND o.payment_method <> 'CASH_ON_DELIVERY') AS awaiting_payment,
                  (SELECT COALESCE(SUM(o.total), 0) FROM store_orders o
                     WHERE o.status = 'PENDING' AND o.payment_method <> 'CASH_ON_DELIVERY') AS awaiting_amount,
                  (SELECT COUNT(*) FROM store_order_seller_groups g JOIN store_orders o ON o.order_id = g.order_id
                     WHERE g.status = 'PENDING' AND o.status <> 'CANCELLED'
                       AND (o.status IN ('PAID', 'PAYMENT_CONFIRMED') OR o.payment_method = 'CASH_ON_DELIVERY')) AS to_prepare,
                  (SELECT COUNT(*) FROM store_order_seller_groups g JOIN store_orders o ON o.order_id = g.order_id
                     WHERE g.status = 'PREPARING' AND o.status <> 'CANCELLED') AS to_ship,
                  (SELECT COUNT(*) FROM store_order_seller_groups g JOIN store_orders o ON o.order_id = g.order_id
                     WHERE g.status = 'SHIPPED' AND o.status <> 'CANCELLED') AS in_transit,
                  (SELECT COUNT(*) FROM store_orders o WHERE o.balance_due IS NOT NULL AND o.status <> 'CANCELLED') AS balances,
                  (SELECT COALESCE(SUM(o.balance_due), 0) FROM store_orders o
                     WHERE o.balance_due IS NOT NULL AND o.status <> 'CANCELLED') AS balances_amount
                """, new MapSqlParameterSource(),
                (rs, i) -> new Operations(rs.getLong("awaiting_payment"), rs.getBigDecimal("awaiting_amount"),
                        rs.getLong("to_prepare"), rs.getLong("to_ship"), rs.getLong("in_transit"),
                        rs.getLong("balances"), rs.getBigDecimal("balances_amount")));
    }

    public Catalog catalog(int lowStockThreshold) {
        return jdbc.queryForObject("""
                SELECT
                  (SELECT COUNT(*) FROM store_products WHERE status = 'ACTIVE') AS active_products,
                  (SELECT COUNT(*) FROM store_products WHERE status = 'OUT_OF_STOCK') AS out_of_stock,
                  (SELECT COUNT(*) FROM store_product_variants v JOIN store_products p ON p.product_id = v.product_id
                     WHERE v.active = 1 AND v.cost IS NULL AND p.status IN ('ACTIVE', 'OUT_OF_STOCK')) AS without_cost,
                  (SELECT COUNT(*) FROM store_product_variants v JOIN store_products p ON p.product_id = v.product_id
                     WHERE v.active = 1 AND v.dropship = 0 AND v.stock <= :threshold
                       AND p.status IN ('ACTIVE', 'OUT_OF_STOCK')) AS low_stock
                """, new MapSqlParameterSource("threshold", lowStockThreshold),
                (rs, i) -> new Catalog(rs.getLong("active_products"), rs.getLong("out_of_stock"),
                        rs.getLong("without_cost"), rs.getLong("low_stock")));
    }

    public Reviews reviews() {
        return jdbc.queryForObject("""
                SELECT
                  (SELECT ROUND(AVG(rating), 2) FROM store_product_reviews WHERE status = 'APROBADO') AS average,
                  (SELECT COUNT(*) FROM store_product_reviews WHERE status = 'APROBADO') AS approved,
                  (SELECT COUNT(*) FROM store_product_reviews WHERE status = 'PENDIENTE') AS pending
                """, new MapSqlParameterSource(),
                (rs, i) -> new Reviews(rs.getBigDecimal("average"), rs.getLong("approved"), rs.getLong("pending")));
    }

    public List<RecentOrder> recentOrders(int limit) {
        return jdbc.query("""
                SELECT o.order_id, o.created_at, o.total, o.status, o.payment_method, o.channel,
                       COALESCE(NULLIF(TRIM(CONCAT_WS(' ', o.guest_name, o.guest_lastname)), ''),
                                TRIM(CONCAT_WS(' ', u.name, u.lastname)), o.buyer_email) AS buyer_name,
                       (SELECT GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', ')
                          FROM store_order_seller_groups g JOIN stores s ON s.store_id = g.store_id
                          WHERE g.order_id = o.order_id) AS stores
                FROM store_orders o
                LEFT JOIN store_profiles sp ON sp.store_profile_id = o.buyer_id
                LEFT JOIN users u ON u.user_id = sp.user_id
                ORDER BY o.created_at DESC, o.order_id DESC
                LIMIT :limit
                """, new MapSqlParameterSource("limit", limit),
                (rs, i) -> new RecentOrder(rs.getLong("order_id"), rs.getString("buyer_name"),
                        toLocal(rs.getTimestamp("created_at")), rs.getBigDecimal("total"), rs.getString("status"),
                        rs.getString("payment_method"), rs.getString("channel"), rs.getString("stores")));
    }

    public SupplierSync lastSupplierSync() {
        List<SupplierSync> runs = jdbc.query("""
                SELECT status, started_at, disabled_count, reenabled_count, cost_updates, margin_alerts, abort_reason
                FROM store_supplier_sync_runs ORDER BY run_id DESC LIMIT 1
                """, new MapSqlParameterSource(),
                (rs, i) -> new SupplierSync(rs.getString("status"), toLocal(rs.getTimestamp("started_at")),
                        rs.getInt("disabled_count"), rs.getInt("reenabled_count"), rs.getInt("cost_updates"),
                        rs.getInt("margin_alerts"), rs.getString("abort_reason")));
        return runs.isEmpty() ? null : runs.get(0);
    }

    private static MapSqlParameterSource range(LocalDateTime from, LocalDateTime to) {
        return new MapSqlParameterSource().addValue("from", Timestamp.valueOf(from)).addValue("to", Timestamp.valueOf(to));
    }

    private static LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
