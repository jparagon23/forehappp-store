package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.BrandFacetResponse;
import com.forehapp.store.productModule.application.dto.CategoryDiscoverySectionResponse;
import com.forehapp.store.productModule.application.dto.PublicProductDetailResponse;
import com.forehapp.store.productModule.application.dto.PublicProductSummaryResponse;
import com.forehapp.store.productModule.domain.model.ProductSortBy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface IPublicProductService {
    Page<PublicProductSummaryResponse> findActiveProducts(String search, Long categoryId, Long brandId, Boolean freeShipping, ProductSortBy sortBy, Pageable pageable, Long storeId, BigDecimal maxPrice, List<Long> excludeProductIds);
    List<BrandFacetResponse> findBrandFacets(String search, Long categoryId, Boolean freeShipping);
    PublicProductDetailResponse findActiveProductById(Long productId);
    List<CategoryDiscoverySectionResponse> findDiscoverySections(int limit);
}
