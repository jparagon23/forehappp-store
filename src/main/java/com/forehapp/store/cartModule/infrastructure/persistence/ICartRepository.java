package com.forehapp.store.cartModule.infrastructure.persistence;

import com.forehapp.store.cartModule.domain.model.Cart;
import com.forehapp.store.cartModule.domain.model.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ICartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.variant v " +
           "LEFT JOIN FETCH v.product p " +
           "LEFT JOIN FETCH p.seller s " +
           "LEFT JOIN FETCH s.user " +
           "WHERE c.buyer.id = :buyerId AND c.status = :status")
    Optional<Cart> findByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") CartStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Cart c SET c.status = com.forehapp.store.cartModule.domain.model.CartStatus.EXPIRED " +
           "WHERE c.status = com.forehapp.store.cartModule.domain.model.CartStatus.ACTIVE " +
           "AND c.updatedAt < :threshold")
    int expireOldCarts(@Param("threshold") LocalDateTime threshold);
}
