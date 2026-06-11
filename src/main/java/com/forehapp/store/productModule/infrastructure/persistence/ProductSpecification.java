package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.model.ProductTag;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVE);
    }

    public static Specification<Product> matchesSearch(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> {
            Subquery<Long> tagSub = query.subquery(Long.class);
            Root<ProductTag> tagRoot = tagSub.from(ProductTag.class);
            tagSub.select(cb.literal(1L))
                    .where(
                            cb.equal(tagRoot.get("product"), root),
                            cb.like(cb.lower(tagRoot.get("tag")), pattern)
                    );
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.join("category").get("description")), pattern),
                    cb.like(cb.lower(root.join("brand").get("description")), pattern),
                    cb.exists(tagSub)
            );
        };
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasBrand(Long brandId) {
        return (root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId);
    }

    public static Specification<Product> hasFreeShipping() {
        return (root, query, cb) -> cb.isTrue(root.get("freeShipping"));
    }

    public static Specification<Product> withPriceOrder(Sort.Direction direction) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                Subquery<BigDecimal> sub = query.subquery(BigDecimal.class);
                Root<ProductVariant> v = sub.from(ProductVariant.class);
                sub.select(cb.min(v.get("price")))
                        .where(cb.equal(v.get("product"), root), cb.isTrue(v.get("active")));
                query.orderBy(direction == Sort.Direction.ASC ? cb.asc(sub) : cb.desc(sub));
            }
            return cb.conjunction();
        };
    }

    public static Specification<Product> hasStore(Long storeId) {
        return (root, query, cb) -> cb.equal(root.get("store").get("id"), storeId);
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            Subquery<BigDecimal> sub = query.subquery(BigDecimal.class);
            Root<ProductVariant> v = sub.from(ProductVariant.class);
            sub.select(cb.min(v.get("price")))
                    .where(cb.equal(v.get("product"), root), cb.isTrue(v.get("active")));
            return cb.lessThanOrEqualTo(sub, maxPrice);
        };
    }

    public static Specification<Product> excludeIds(List<Long> ids) {
        return (root, query, cb) -> root.get("id").in(ids).not();
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
