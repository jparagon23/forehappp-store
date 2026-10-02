package com.forehapp.store.repurchaseModule.infrastructure.persistence;

import com.forehapp.store.repurchaseModule.domain.model.PurchaseRow;
import com.forehapp.store.repurchaseModule.domain.model.RepurchaseReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface IRepurchaseReminderJpaRepository extends JpaRepository<RepurchaseReminder, Long> {

    // A purchase counts once it is paid or the seller already started fulfilling it (cash on delivery
    // orders stay PENDING until delivered). Products are included when the product or any of its
    // variants has a duration, so a later purchase of any variant is seen as a repurchase.
    @Query("SELECT new com.forehapp.store.repurchaseModule.domain.model.PurchaseRow(" +
           "i.id, o.id, o.createdAt, o.buyerEmail, o.guestName, u.name, i.quantity, " +
           "v.id, v.repurchaseDays, v.active, v.stock, v.price, " +
           "p.id, p.title, p.repurchaseDays, p.status, g.deliveredAt, g.shippedAt) " +
           "FROM OrderItem i " +
           "JOIN i.sellerGroup g " +
           "JOIN g.order o " +
           "JOIN i.variant v " +
           "JOIN v.product p " +
           "LEFT JOIN o.buyer b " +
           "LEFT JOIN b.user u " +
           "WHERE o.createdAt >= :since " +
           "AND o.buyerEmail IS NOT NULL " +
           "AND o.status <> com.forehapp.store.orderModule.domain.model.OrderStatus.CANCELLED " +
           "AND g.status <> com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus.CANCELLED " +
           "AND (o.status IN (com.forehapp.store.orderModule.domain.model.OrderStatus.PAID, " +
           "                  com.forehapp.store.orderModule.domain.model.OrderStatus.PAYMENT_CONFIRMED) " +
           "     OR g.status <> com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus.PENDING) " +
           "AND (p.repurchaseDays IS NOT NULL " +
           "     OR EXISTS (SELECT 1 FROM ProductVariant v2 WHERE v2.product = p AND v2.repurchaseDays IS NOT NULL))")
    List<PurchaseRow> findPurchasesSince(@Param("since") LocalDateTime since);

    @Query("SELECT r.orderItemId FROM RepurchaseReminder r WHERE r.orderItemId IN :ids")
    List<Long> findRemindedOrderItemIds(@Param("ids") Collection<Long> ids);

    List<RepurchaseReminder> findByEmailIn(Collection<String> emails);

    @Query("SELECT LOWER(o.buyerEmail), MAX(o.createdAt) FROM Order o " +
           "WHERE LOWER(o.buyerEmail) IN :emails " +
           "AND o.status <> com.forehapp.store.orderModule.domain.model.OrderStatus.CANCELLED " +
           "AND (o.status IN (com.forehapp.store.orderModule.domain.model.OrderStatus.PAID, " +
           "                  com.forehapp.store.orderModule.domain.model.OrderStatus.PAYMENT_CONFIRMED) " +
           "     OR EXISTS (SELECT 1 FROM OrderSellerGroup g WHERE g.order = o " +
           "                AND g.status NOT IN (com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus.PENDING, " +
           "                                     com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus.CANCELLED))) " +
           "GROUP BY LOWER(o.buyerEmail)")
    List<Object[]> findLastOrderAtByEmails(@Param("emails") Collection<String> emails);

    @Query("SELECT img.product.id, img.url FROM ProductImage img " +
           "WHERE img.product.id IN :productIds ORDER BY img.displayOrder ASC, img.id ASC")
    List<Object[]> findImageUrlsByProductIds(@Param("productIds") Collection<Long> productIds);
}
