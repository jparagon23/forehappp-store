package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IInventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock + :delta WHERE v.id = :variantId")
    void incrementStock(@Param("variantId") Long variantId, @Param("delta") int delta);
}
