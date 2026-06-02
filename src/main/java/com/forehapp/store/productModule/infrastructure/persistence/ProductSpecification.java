package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVE);
    }

    public static Specification<Product> matchesSearch(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.join("category").get("description")), pattern),
                cb.like(cb.lower(root.join("brand").get("description")), pattern)
        );
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasBrand(Long brandId) {
        return (root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId);
    }

    public static Specification<Product> withDiscoveryOrder() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                int seed = LocalDate.now().getDayOfYear();
                Expression<Long> hash = cb.function("CRC32", Long.class,
                        cb.concat(root.get("id").as(String.class), cb.literal(String.valueOf(seed))));
                query.orderBy(cb.asc(hash));
            }
            return cb.conjunction();
        };
    }
}
