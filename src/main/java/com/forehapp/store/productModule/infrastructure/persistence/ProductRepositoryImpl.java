package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.BrandCount;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductDiscoverySection;
import com.forehapp.store.productModule.domain.model.ProductSortBy;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.model.ProductTag;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductRepositoryImpl implements IProductDao {

    private final IProductRepository jpaRepository;
    private final EntityManager entityManager;

    public ProductRepositoryImpl(IProductRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Product> findByIdAndStoreId(Long productId, Long storeId) {
        return jpaRepository.findByIdAndStoreId(productId, storeId);
    }

    @Override
    public List<Product> findAllByStoreId(Long storeId) {
        return jpaRepository.findAllByStoreId(storeId);
    }

    @Override
    public Page<Product> findActiveProducts(String search, Long categoryId, Long brandId, Boolean freeShipping, ProductSortBy sortBy, Pageable pageable) {
        if (sortBy == ProductSortBy.DISCOVERY && search == null && categoryId == null && brandId == null && !Boolean.TRUE.equals(freeShipping)) {
            return findDiscoveryProducts(pageable);
        }

        Specification<Product> spec = Specification.where(ProductSpecification.isActive());
        if (search != null && !search.isBlank()) {
            spec = spec.and(ProductSpecification.matchesSearch(search));
        }
        if (categoryId != null) {
            spec = spec.and(ProductSpecification.hasCategory(categoryId));
        }
        if (brandId != null) {
            spec = spec.and(ProductSpecification.hasBrand(brandId));
        }
        if (Boolean.TRUE.equals(freeShipping)) {
            spec = spec.and(ProductSpecification.hasFreeShipping());
        }
        if (sortBy == ProductSortBy.PRICE_ASC) {
            spec = spec.and(ProductSpecification.withPriceOrder(Sort.Direction.ASC));
        } else if (sortBy == ProductSortBy.PRICE_DESC) {
            spec = spec.and(ProductSpecification.withPriceOrder(Sort.Direction.DESC));
        }
        return jpaRepository.findAll(spec, pageable);
    }

    private Page<Product> findDiscoveryProducts(Pageable pageable) {
        int seed = LocalDate.now().getDayOfYear();
        Pageable idPage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<Long> ids = jpaRepository.findDiscoveryProductIds(seed, idPage);
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        long total = jpaRepository.countActiveStoreProducts();

        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) order.put(ids.get(i), i);

        List<Product> products = jpaRepository.findByIdInWithGraph(ids)
                .stream()
                .sorted(Comparator.comparingInt(p -> order.get(p.getId())))
                .toList();

        return new PageImpl<>(products, pageable, total);
    }

    @Override
    public List<BrandCount> findBrandFacets(String search, Long categoryId, Boolean freeShipping) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Product> root = query.from(Product.class);
        Join<Object, Object> brandJoin = root.join("brand");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            Join<Object, Object> categoryJoin = root.join("category", JoinType.LEFT);

            Subquery<Long> tagSub = query.subquery(Long.class);
            Root<ProductTag> tagRoot = tagSub.from(ProductTag.class);
            tagSub.select(cb.literal(1L))
                    .where(
                            cb.equal(tagRoot.get("product"), root),
                            cb.like(cb.lower(tagRoot.get("tag")), pattern)
                    );

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(brandJoin.get("description")), pattern),
                    cb.like(cb.lower(categoryJoin.get("description")), pattern),
                    cb.exists(tagSub)
            ));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        }
        if (Boolean.TRUE.equals(freeShipping)) {
            predicates.add(cb.isTrue(root.get("freeShipping")));
        }

        Expression<Long> countExpr = cb.count(root);
        query.multiselect(brandJoin.get("id"), brandJoin.get("description"), countExpr)
                .where(predicates.toArray(new Predicate[0]))
                .groupBy(brandJoin.get("id"), brandJoin.get("description"))
                .orderBy(cb.desc(countExpr));

        return entityManager.createQuery(query).getResultList().stream()
                .map(row -> new BrandCount((Long) row[0], (String) row[1], (Long) row[2]))
                .toList();
    }

    @Override
    public List<ProductDiscoverySection> findDiscoverySections(int limit) {
        int seed = LocalDate.now().getDayOfYear();
        List<DiscoveryProductIdView> entries = jpaRepository.findDiscoverySectionIds(seed, limit);
        if (entries.isEmpty()) return List.of();

        List<Long> ids = entries.stream().map(DiscoveryProductIdView::getProductId).toList();
        Map<Long, Product> productMap = jpaRepository.findByIdInWithGraph(ids)
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        Map<Long, List<Product>> byCategory = new LinkedHashMap<>();
        Map<Long, Long> categoryTotals = new LinkedHashMap<>();
        for (DiscoveryProductIdView entry : entries) {
            Product product = productMap.get(entry.getProductId());
            if (product == null) continue;
            byCategory.computeIfAbsent(entry.getCategoryId(), k -> new ArrayList<>()).add(product);
            categoryTotals.put(entry.getCategoryId(), entry.getCatTotal());
        }

        return byCategory.entrySet().stream()
                .map(e -> new ProductDiscoverySection(e.getKey(), categoryTotals.get(e.getKey()), e.getValue()))
                .toList();
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public void delete(Product product) {
        jpaRepository.delete(product);
    }
}
