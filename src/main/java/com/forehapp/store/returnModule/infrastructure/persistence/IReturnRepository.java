package com.forehapp.store.returnModule.infrastructure.persistence;

import com.forehapp.store.returnModule.domain.model.ReturnRequest;
import com.forehapp.store.returnModule.domain.model.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IReturnRepository extends JpaRepository<ReturnRequest, Long> {

    @Query("SELECT r FROM ReturnRequest r JOIN FETCH r.orderGroup g JOIN FETCH g.order o JOIN FETCH o.buyer b JOIN FETCH b.user JOIN FETCH r.buyer rb JOIN FETCH rb.user LEFT JOIN FETCH r.items i LEFT JOIN FETCH i.orderItem oi LEFT JOIN FETCH oi.variant v LEFT JOIN FETCH v.product WHERE r.id = :id")
    Optional<ReturnRequest> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM ReturnRequest r JOIN FETCH r.orderGroup g JOIN FETCH g.order o JOIN FETCH o.buyer b JOIN FETCH b.user JOIN FETCH r.buyer rb JOIN FETCH rb.user LEFT JOIN FETCH r.items i LEFT JOIN FETCH i.orderItem oi LEFT JOIN FETCH oi.variant v LEFT JOIN FETCH v.product WHERE g.id = :groupId")
    Optional<ReturnRequest> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT r FROM ReturnRequest r JOIN FETCH r.orderGroup g JOIN FETCH g.order o JOIN FETCH o.buyer b JOIN FETCH b.user JOIN FETCH r.buyer rb JOIN FETCH rb.user LEFT JOIN FETCH r.items i LEFT JOIN FETCH i.orderItem oi LEFT JOIN FETCH oi.variant v LEFT JOIN FETCH v.product WHERE r.buyer.id = :buyerId ORDER BY r.createdAt DESC")
    List<ReturnRequest> findByBuyerId(@Param("buyerId") Long buyerId);

    @Query(value = "SELECT r FROM ReturnRequest r JOIN FETCH r.orderGroup g JOIN FETCH g.order o JOIN FETCH o.buyer b JOIN FETCH b.user JOIN FETCH r.buyer rb JOIN FETCH rb.user LEFT JOIN FETCH r.items i LEFT JOIN FETCH i.orderItem oi LEFT JOIN FETCH oi.variant v LEFT JOIN FETCH v.product WHERE r.status = :status",
           countQuery = "SELECT COUNT(r) FROM ReturnRequest r WHERE r.status = :status")
    Page<ReturnRequest> findByStatus(@Param("status") ReturnStatus status, Pageable pageable);
}
