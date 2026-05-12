package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.CreateProductRequestDto;
import com.forehapp.store.productModule.application.dto.CreateVariantDto;
import com.forehapp.store.productModule.application.dto.ProductResponse;
import com.forehapp.store.productModule.application.dto.ProductVariantResponse;
import com.forehapp.store.productModule.application.dto.SellerProductDetailResponse;
import com.forehapp.store.productModule.application.dto.UpdateProductRequestDto;
import com.forehapp.store.productModule.application.dto.UpdateVariantDto;

import java.util.List;

public interface IProductService {
    ProductResponse createProduct(CreateProductRequestDto dto, Long userId);
    ProductResponse updateProduct(Long productId, UpdateProductRequestDto dto, Long userId);
    ProductVariantResponse addVariant(Long productId, CreateVariantDto dto, Long userId);
    ProductVariantResponse updateVariant(Long productId, Long variantId, UpdateVariantDto dto, Long userId);
    ProductResponse publish(Long productId, Long userId);
    ProductResponse activate(Long productId, Long userId);
    ProductResponse deactivate(Long productId, Long userId);
    List<ProductResponse> getSellerProducts(Long userId);
    SellerProductDetailResponse getSellerProductById(Long productId, Long userId);
    void deleteProduct(Long productId, Long userId);
    void deleteVariant(Long productId, Long variantId, Long userId);
}
