package com.forehapp.store.orderModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IOrderSellerGroupRepository extends JpaRepository<OrderSellerGroup, Long> {

    @Query("""
            SELECT DISTINCT g FROM OrderSellerGroup g
            LEFT JOIN FETCH g.order o
            LEFT JOIN FETCH o.buyer b
            LEFT JOIN FETCH b.user
            LEFT JOIN FETCH g.items i
            LEFT JOIN FETCH i.variant v
            LEFT JOIN FETCH v.product p
            JOIN FETCH p.category
            JOIN FETCH p.brand
            LEFT JOIN FETCH p.line
            WHERE g.id = :groupId
            """)
    Optional<OrderSellerGroup> findByIdWithDetails(@Param("groupId") Long groupId);

    @Query("""
            SELECT DISTINCT g FROM OrderSellerGroup g
            LEFT JOIN FETCH g.order o
            LEFT JOIN FETCH o.buyer b
            LEFT JOIN FETCH b.user
            LEFT JOIN FETCH g.items i
            LEFT JOIN FETCH i.variant v
            LEFT JOIN FETCH v.product p
            JOIN FETCH p.category
            JOIN FETCH p.brand
            LEFT JOIN FETCH p.line
            WHERE g.store.id = :storeId
              AND g.status <> com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus.PENDING
            ORDER BY o.createdAt DESC
            """)
    List<OrderSellerGroup> findAllByStoreIdWithDetails(@Param("storeId") Long storeId);

    List<OrderSellerGroup> findAllByOrder_Id(Long orderId);
}
