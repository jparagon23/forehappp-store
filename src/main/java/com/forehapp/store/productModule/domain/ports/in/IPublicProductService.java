package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.PublicProductDetailResponse;
import com.forehapp.store.productModule.application.dto.PublicProductSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPublicProductService {
    Page<PublicProductSummaryResponse> findActiveProducts(String search, Long categoryId, Long brandId, Pageable pageable);
    PublicProductDetailResponse findActiveProductById(Long productId);
}
