package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.CategoryDiscoverySectionResponse;
import com.forehapp.store.productModule.application.dto.PublicProductDetailResponse;
import com.forehapp.store.productModule.application.dto.PublicProductSummaryResponse;
import com.forehapp.store.productModule.domain.model.ProductSortBy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPublicProductService {
    Page<PublicProductSummaryResponse> findActiveProducts(String search, Long categoryId, Long brandId, ProductSortBy sortBy, Pageable pageable);
    PublicProductDetailResponse findActiveProductById(Long productId);
    List<CategoryDiscoverySectionResponse> findDiscoverySections(int limit);
}
