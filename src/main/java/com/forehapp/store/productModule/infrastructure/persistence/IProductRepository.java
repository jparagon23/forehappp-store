package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByBrandId(Long brandId);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByLineId(Long lineId);

    @EntityGraph(attributePaths = {"brand", "category", "line", "store"})
    List<Product> findAllByStoreId(Long storeId);

    @Override
    @EntityGraph(attributePaths = {"brand", "category", "line"})
    @NonNull
    Page<Product> findAll(Specification<Product> spec, @NonNull Pageable pageable);

    @Query(value = """
            SELECT ranked.product_id FROM (
                SELECT p.product_id,
                       ROW_NUMBER() OVER (PARTITION BY p.category_id ORDER BY CRC32(CONCAT(p.product_id, :seed))) AS cat_rank,
                       p.category_id
                FROM store_products p
                WHERE p.status = 'ACTIVE'
                  AND EXISTS (
                      SELECT 1 FROM store_product_variants pv
                      WHERE pv.product_id = p.product_id
                        AND pv.active = TRUE
                        AND pv.stock > 0
                  )
            ) ranked
            ORDER BY ranked.cat_rank, CRC32(CONCAT(ranked.category_id, :seed))
            """, nativeQuery = true)
    List<Long> findDiscoveryProductIds(@Param("seed") int seed, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "category", "line"})
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findByIdInWithGraph(@Param("ids") Collection<Long> ids);

    @Query(value = "SELECT COUNT(*) FROM store_products WHERE status = 'ACTIVE'", nativeQuery = true)
    long countActiveStoreProducts();

    @Query(value = """
            SELECT ranked.product_id AS productId, ranked.category_id AS categoryId, ranked.cat_total AS catTotal
            FROM (
                SELECT p.product_id, p.category_id,
                       ROW_NUMBER() OVER (PARTITION BY p.category_id ORDER BY CRC32(CONCAT(p.product_id, :seed))) AS rn,
                       COUNT(*) OVER (PARTITION BY p.category_id) AS cat_total
                FROM store_products p
                WHERE p.status = 'ACTIVE'
                  AND EXISTS (
                      SELECT 1 FROM store_product_variants pv
                      WHERE pv.product_id = p.product_id
                        AND pv.active = TRUE
                        AND pv.stock > 0
                  )
            ) ranked
            WHERE ranked.rn <= :limit
            ORDER BY ranked.cat_total DESC, ranked.category_id, ranked.rn
            """, nativeQuery = true)
    List<DiscoveryProductIdView> findDiscoverySectionIds(@Param("seed") int seed, @Param("limit") int limit);
}
