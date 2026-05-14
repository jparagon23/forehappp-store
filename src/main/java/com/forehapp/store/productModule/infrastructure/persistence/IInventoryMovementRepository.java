package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.InventoryMovement;
import com.forehapp.store.productModule.domain.model.MovementReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IInventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock + :delta WHERE v.id = :variantId")
    void incrementStock(@Param("variantId") Long variantId, @Param("delta") int delta);

    @Query("SELECT m FROM InventoryMovement m WHERE m.variant.id = :variantId " +
           "AND (:reason IS NULL OR m.reason = :reason) " +
           "ORDER BY m.createdAt DESC")
    Page<InventoryMovement> findByVariant(@Param("variantId") Long variantId,
                                          @Param("reason") MovementReason reason,
                                          Pageable pageable);
}
