package com.forehapp.store.supplierSyncModule.infrastructure.persistence;

import com.forehapp.store.supplierSyncModule.domain.model.LinkSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.OrderAtRisk;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLink;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLinkStatus;
import com.forehapp.store.supplierSyncModule.domain.model.VariantRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ISupplierLinkJpaRepository extends JpaRepository<SupplierLink, Long> {

    List<SupplierLink> findByVariantIdAndSupplier(Long variantId, SupplierCode supplier);

    List<SupplierLink> findByVariantIdInAndSupplier(Collection<Long> variantIds, SupplierCode supplier);

    @Query("SELECT new com.forehapp.store.supplierSyncModule.domain.model.LinkSnapshot(" +
           "l.id, v.id, p.id, p.title, v.sku, v.active, v.stock, v.dropship, v.supplierAvailable, v.price, v.cost, " +
           "l.status, l.supplierItemId) " +
           "FROM SupplierLink l, ProductVariant v JOIN v.product p " +
           "WHERE v.id = l.variantId AND l.storeId = :storeId AND l.supplier = :supplier " +
           "AND p.store.id = :storeId AND l.status IN :statuses")
    List<LinkSnapshot> findSnapshots(@Param("storeId") Long storeId,
                                     @Param("supplier") SupplierCode supplier,
                                     @Param("statuses") Collection<SupplierLinkStatus> statuses);

    @Query("SELECT l FROM SupplierLink l, ProductVariant v JOIN v.product p " +
           "WHERE v.id = l.variantId AND l.storeId = :storeId AND l.supplier = :supplier " +
           "AND p.store.id = :storeId AND l.status = :status ORDER BY p.title ASC, v.id ASC")
    List<SupplierLink> findByStoreAndStatus(@Param("storeId") Long storeId,
                                            @Param("supplier") SupplierCode supplier,
                                            @Param("status") SupplierLinkStatus status);

    // Variants with no CONFIRMED, SUGGESTED or NOT_SUPPLIED pair (rejected-only variants count as unpaired)
    @Query("SELECT new com.forehapp.store.supplierSyncModule.domain.model.VariantRow(" +
           "v.id, p.id, p.store.id, p.title, b.description, v.sku, v.active, v.stock, v.dropship, v.supplierAvailable, v.price) " +
           "FROM ProductVariant v JOIN v.product p JOIN p.brand b " +
           "WHERE p.store.id IN :storeIds " +
           "AND NOT EXISTS (SELECT 1 FROM SupplierLink l WHERE l.variantId = v.id AND l.supplier = :supplier " +
           "                AND l.status IN :statuses) " +
           "ORDER BY p.title ASC, v.id ASC")
    List<VariantRow> findVariantsWithoutLink(@Param("storeIds") Collection<Long> storeIds,
                                             @Param("supplier") SupplierCode supplier,
                                             @Param("statuses") Collection<SupplierLinkStatus> statuses);

    @Query("SELECT new com.forehapp.store.supplierSyncModule.domain.model.VariantRow(" +
           "v.id, p.id, p.store.id, p.title, b.description, v.sku, v.active, v.stock, v.dropship, v.supplierAvailable, v.price) " +
           "FROM ProductVariant v JOIN v.product p JOIN p.brand b WHERE v.id IN :variantIds")
    List<VariantRow> findVariantRows(@Param("variantIds") Collection<Long> variantIds);

    @Query("SELECT v.id, a.description, av.description FROM ProductVariant v " +
           "JOIN v.attributeValues av JOIN av.attribute a WHERE v.id IN :variantIds")
    List<Object[]> findVariantAttributes(@Param("variantIds") Collection<Long> variantIds);

    // Only units still to be ordered from the supplier are at risk; own stock was already set aside
    @Query("SELECT new com.forehapp.store.supplierSyncModule.domain.model.OrderAtRisk(o.id, i.variant.id, i.dropshipQuantity) " +
           "FROM OrderItem i JOIN i.sellerGroup g JOIN g.order o " +
           "WHERE i.variant.id IN :variantIds AND i.dropshipQuantity > 0 " +
           "AND o.status <> com.forehapp.store.orderModule.domain.model.OrderStatus.CANCELLED " +
           "AND g.status IN (com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus.PENDING, " +
           "                 com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus.PREPARING) " +
           "ORDER BY o.id ASC")
    List<OrderAtRisk> findOpenOrdersWithVariants(@Param("variantIds") Collection<Long> variantIds);
}
