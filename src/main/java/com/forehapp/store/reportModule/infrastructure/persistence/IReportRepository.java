package com.forehapp.store.reportModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IReportRepository extends JpaRepository<Order, Long> {

    // ── Admin: summary ────────────────────────────────────────────────────────

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    Long countOrdersByStatus(@Param("status") String status,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueByStatus(@Param("status") String status,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(AVG(o.total), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    BigDecimal avgTicketByStatus(@Param("status") String status,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(r) FROM ReturnRequest r WHERE r.status IN ('APROBADA', 'REEMBOLSADA') AND r.createdAt BETWEEN :from AND :to")
    Long countApprovedReturns(@Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM ReturnRequest r WHERE r.status = 'REEMBOLSADA' AND r.createdAt BETWEEN :from AND :to")
    BigDecimal sumRefunded(@Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    // ── Admin: revenue by period ──────────────────────────────────────────────

    @Query(value = """
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS period,
                   COUNT(*)                             AS orderCount,
                   SUM(total)                           AS revenue
            FROM store_orders
            WHERE status = 'PAID' AND created_at BETWEEN :from AND :to
            GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
            ORDER BY period
            """, nativeQuery = true)
    List<RevenueProjection> getRevenueByDay(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT DATE_FORMAT(created_at, '%Y-%u') AS period,
                   COUNT(*)                          AS orderCount,
                   SUM(total)                        AS revenue
            FROM store_orders
            WHERE status = 'PAID' AND created_at BETWEEN :from AND :to
            GROUP BY DATE_FORMAT(created_at, '%Y-%u')
            ORDER BY period
            """, nativeQuery = true)
    List<RevenueProjection> getRevenueByWeek(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT DATE_FORMAT(created_at, '%Y-%m') AS period,
                   COUNT(*)                          AS orderCount,
                   SUM(total)                        AS revenue
            FROM store_orders
            WHERE status = 'PAID' AND created_at BETWEEN :from AND :to
            GROUP BY DATE_FORMAT(created_at, '%Y-%m')
            ORDER BY period
            """, nativeQuery = true)
    List<RevenueProjection> getRevenueByMonth(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    // ── Admin: top products ───────────────────────────────────────────────────

    @Query(value = """
            SELECT p.product_id     AS productId,
                   p.title          AS productTitle,
                   pv.sku           AS variantSku,
                   SUM(oi.quantity) AS unitsSold,
                   SUM(oi.quantity * oi.unit_price) AS revenue
            FROM store_order_items oi
            INNER JOIN store_order_seller_groups osg ON oi.group_id   = osg.group_id
            INNER JOIN store_orders              o   ON osg.order_id  = o.order_id
            INNER JOIN store_product_variants    pv  ON oi.variant_id = pv.variant_id
            INNER JOIN store_products            p   ON pv.product_id = p.product_id
            WHERE o.status = 'PAID' AND o.created_at BETWEEN :from AND :to
            GROUP BY p.product_id, p.title, pv.sku
            ORDER BY unitsSold DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopProductProjection> getTopProducts(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to,
                                               @Param("limit") int limit);

    // ── Admin: seller sales ───────────────────────────────────────────────────

    @Query(value = """
            SELECT sp.store_profile_id                     AS sellerId,
                   CONCAT(u.name, ' ', u.lastname)        AS sellerName,
                   COUNT(DISTINCT o.order_id)             AS totalOrders,
                   SUM(osg.subtotal)                      AS totalRevenue
            FROM store_order_seller_groups osg
            INNER JOIN store_orders   o  ON osg.order_id = o.order_id
            INNER JOIN store_profiles sp ON osg.seller_id = sp.store_profile_id
            INNER JOIN users          u  ON sp.user_id   = u.user_id
            WHERE o.status = 'PAID' AND o.created_at BETWEEN :from AND :to
            GROUP BY sp.store_profile_id, u.name, u.lastname
            ORDER BY totalRevenue DESC
            """, nativeQuery = true)
    List<SellerSalesProjection> getSellerSales(@Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);

    // ── Seller: summary ───────────────────────────────────────────────────────

    @Query("SELECT COUNT(DISTINCT g.order) FROM OrderSellerGroup g WHERE g.seller.id = :sellerId AND g.order.status = 'PAID' AND g.order.createdAt BETWEEN :from AND :to")
    Long countSellerOrders(@Param("sellerId") Long sellerId,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(g.subtotal), 0) FROM OrderSellerGroup g WHERE g.seller.id = :sellerId AND g.order.status = 'PAID' AND g.order.createdAt BETWEEN :from AND :to")
    BigDecimal sumSellerRevenue(@Param("sellerId") Long sellerId,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(AVG(g.subtotal), 0) FROM OrderSellerGroup g WHERE g.seller.id = :sellerId AND g.order.status = 'PAID' AND g.order.createdAt BETWEEN :from AND :to")
    BigDecimal avgSellerTicket(@Param("sellerId") Long sellerId,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(r) FROM ReturnRequest r JOIN r.orderGroup g WHERE g.seller.id = :sellerId AND r.status IN ('APROBADA', 'REEMBOLSADA') AND r.createdAt BETWEEN :from AND :to")
    Long countSellerReturns(@Param("sellerId") Long sellerId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM ReturnRequest r JOIN r.orderGroup g WHERE g.seller.id = :sellerId AND r.status = 'REEMBOLSADA' AND r.createdAt BETWEEN :from AND :to")
    BigDecimal sumSellerRefunded(@Param("sellerId") Long sellerId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    // ── Seller: top products ──────────────────────────────────────────────────

    @Query(value = """
            SELECT p.product_id     AS productId,
                   p.title          AS productTitle,
                   pv.sku           AS variantSku,
                   SUM(oi.quantity) AS unitsSold,
                   SUM(oi.quantity * oi.unit_price) AS revenue
            FROM store_order_items oi
            INNER JOIN store_order_seller_groups osg ON oi.group_id   = osg.group_id
            INNER JOIN store_orders              o   ON osg.order_id  = o.order_id
            INNER JOIN store_product_variants    pv  ON oi.variant_id = pv.variant_id
            INNER JOIN store_products            p   ON pv.product_id = p.product_id
            WHERE o.status = 'PAID' AND osg.seller_id = :sellerId AND o.created_at BETWEEN :from AND :to
            GROUP BY p.product_id, p.title, pv.sku
            ORDER BY unitsSold DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopProductProjection> getSellerTopProducts(@Param("sellerId") Long sellerId,
                                                     @Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to,
                                                     @Param("limit") int limit);

    // ── Projections ───────────────────────────────────────────────────────────

    interface RevenueProjection {
        String getPeriod();
        Long getOrderCount();
        java.math.BigDecimal getRevenue();
    }

    interface TopProductProjection {
        Long getProductId();
        String getProductTitle();
        String getVariantSku();
        Long getUnitsSold();
        java.math.BigDecimal getRevenue();
    }

    interface SellerSalesProjection {
        Long getSellerId();
        String getSellerName();
        Long getTotalOrders();
        java.math.BigDecimal getTotalRevenue();
    }
}
