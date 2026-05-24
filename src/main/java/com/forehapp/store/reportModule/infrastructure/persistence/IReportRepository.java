package com.forehapp.store.reportModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IReportRepository extends JpaRepository<Order, Long> {

    // ── Admin: summary ────────────────────────────────────────────────────────

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    Long countOrdersByStatus(@Param("status") OrderStatus status,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueByStatus(@Param("status") OrderStatus status,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(AVG(o.total), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    BigDecimal avgTicketByStatus(@Param("status") OrderStatus status,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(r) FROM com.forehapp.store.returnModule.domain.model.ReturnRequest r " +
           "WHERE r.status IN (com.forehapp.store.returnModule.domain.model.ReturnStatus.APROBADA, " +
           "com.forehapp.store.returnModule.domain.model.ReturnStatus.REEMBOLSADA) " +
           "AND r.createdAt BETWEEN :from AND :to")
    Long countApprovedReturns(@Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM com.forehapp.store.returnModule.domain.model.ReturnRequest r " +
           "WHERE r.status = com.forehapp.store.returnModule.domain.model.ReturnStatus.REEMBOLSADA " +
           "AND r.createdAt BETWEEN :from AND :to")
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

    // ── Admin: store sales ────────────────────────────────────────────────────

    @Query(value = """
            SELECT s.store_id              AS storeId,
                   s.name                 AS storeName,
                   COUNT(DISTINCT o.order_id) AS totalOrders,
                   SUM(osg.subtotal)      AS totalRevenue
            FROM store_order_seller_groups osg
            INNER JOIN store_orders o ON osg.order_id = o.order_id
            INNER JOIN stores       s ON osg.store_id = s.store_id
            WHERE o.status = 'PAID' AND o.created_at BETWEEN :from AND :to
            GROUP BY s.store_id, s.name
            ORDER BY totalRevenue DESC
            """, nativeQuery = true)
    List<SellerSalesProjection> getSellerSales(@Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);

    // ── Seller: summary ───────────────────────────────────────────────────────

    @Query("SELECT COUNT(DISTINCT g.order) FROM com.forehapp.store.orderModule.domain.model.OrderSellerGroup g " +
           "WHERE g.store.id = :storeId " +
           "AND g.order.status = com.forehapp.store.orderModule.domain.model.OrderStatus.PAID " +
           "AND g.order.createdAt BETWEEN :from AND :to")
    Long countSellerOrders(@Param("storeId") Long storeId,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(g.subtotal), 0) FROM com.forehapp.store.orderModule.domain.model.OrderSellerGroup g " +
           "WHERE g.store.id = :storeId " +
           "AND g.order.status = com.forehapp.store.orderModule.domain.model.OrderStatus.PAID " +
           "AND g.order.createdAt BETWEEN :from AND :to")
    BigDecimal sumSellerRevenue(@Param("storeId") Long storeId,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(AVG(g.subtotal), 0) FROM com.forehapp.store.orderModule.domain.model.OrderSellerGroup g " +
           "WHERE g.store.id = :storeId " +
           "AND g.order.status = com.forehapp.store.orderModule.domain.model.OrderStatus.PAID " +
           "AND g.order.createdAt BETWEEN :from AND :to")
    BigDecimal avgSellerTicket(@Param("storeId") Long storeId,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(r) FROM com.forehapp.store.returnModule.domain.model.ReturnRequest r " +
           "JOIN r.orderGroup g " +
           "WHERE g.store.id = :storeId " +
           "AND r.status IN (com.forehapp.store.returnModule.domain.model.ReturnStatus.APROBADA, " +
           "com.forehapp.store.returnModule.domain.model.ReturnStatus.REEMBOLSADA) " +
           "AND r.createdAt BETWEEN :from AND :to")
    Long countSellerReturns(@Param("storeId") Long storeId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM com.forehapp.store.returnModule.domain.model.ReturnRequest r " +
           "JOIN r.orderGroup g " +
           "WHERE g.store.id = :storeId " +
           "AND r.status = com.forehapp.store.returnModule.domain.model.ReturnStatus.REEMBOLSADA " +
           "AND r.createdAt BETWEEN :from AND :to")
    BigDecimal sumSellerRefunded(@Param("storeId") Long storeId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    // ── Seller: group status counts ──────────────────────────────────────────

    @Query("SELECT COUNT(g) FROM com.forehapp.store.orderModule.domain.model.OrderSellerGroup g " +
           "WHERE g.store.id = :storeId " +
           "AND g.status = :status " +
           "AND g.order.status != com.forehapp.store.orderModule.domain.model.OrderStatus.CANCELLED " +
           "AND g.order.createdAt BETWEEN :from AND :to")
    Long countSellerGroupsByStatus(@Param("storeId") Long storeId,
                                    @Param("status") OrderSellerGroupStatus status,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    // ── Seller: low stock ─────────────────────────────────────────────────────

    @Query(value = """
            SELECT pv.variant_id AS variantId,
                   p.product_id  AS productId,
                   p.title       AS productTitle,
                   pv.sku        AS sku,
                   pv.stock      AS stock
            FROM store_product_variants pv
            INNER JOIN store_products p ON pv.product_id = p.product_id
            WHERE p.store_id = :storeId
              AND pv.stock <= :threshold
            ORDER BY pv.stock ASC
            """, nativeQuery = true)
    List<LowStockProjection> getLowStockByStore(@Param("storeId") Long storeId,
                                                 @Param("threshold") int threshold);

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
            WHERE o.status = 'PAID' AND osg.store_id = :storeId AND o.created_at BETWEEN :from AND :to
            GROUP BY p.product_id, p.title, pv.sku
            ORDER BY unitsSold DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopProductProjection> getSellerTopProducts(@Param("storeId") Long storeId,
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
        Long getStoreId();
        String getStoreName();
        Long getTotalOrders();
        java.math.BigDecimal getTotalRevenue();
    }

    interface LowStockProjection {
        Long getVariantId();
        Long getProductId();
        String getProductTitle();
        String getSku();
        Integer getStock();
    }
}
