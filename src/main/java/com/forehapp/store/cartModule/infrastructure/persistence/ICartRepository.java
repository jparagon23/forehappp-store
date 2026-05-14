package com.forehapp.store.cartModule.infrastructure.persistence;

import com.forehapp.store.cartModule.domain.model.Cart;
import com.forehapp.store.cartModule.domain.model.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
