package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.CreateProductRequestDto;
import com.forehapp.store.productModule.application.dto.CreateVariantDto;
import com.forehapp.store.productModule.application.dto.ProductResponse;
import com.forehapp.store.productModule.application.dto.ProductVariantResponse;

public interface IProductService {
    ProductResponse createProduct(CreateProductRequestDto dto, Long userId);
    ProductVariantResponse addVariant(Long productId, CreateVariantDto dto, Long userId);
    ProductResponse publish(Long productId, Long userId);
    ProductResponse activate(Long productId, Long userId);
    ProductResponse deactivate(Long productId, Long userId);
}
