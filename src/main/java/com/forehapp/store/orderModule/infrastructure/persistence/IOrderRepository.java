package com.forehapp.store.orderModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IOrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.sellerGroups sg " +
           "LEFT JOIN FETCH sg.seller sel " +
           "LEFT JOIN FETCH sel.user " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithGroups(@Param("orderId") Long orderId);

    List<Order> findAllByBuyer_IdOrderByCreatedAtDesc(Long buyerId);
}
