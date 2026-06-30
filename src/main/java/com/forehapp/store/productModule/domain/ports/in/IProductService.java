package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.CreateProductRequestDto;
import com.forehapp.store.productModule.application.dto.CreateVariantDto;
import com.forehapp.store.productModule.application.dto.ProductResponse;
import com.forehapp.store.productModule.application.dto.ProductVariantResponse;
import com.forehapp.store.productModule.application.dto.SellerProductDetailResponse;
import com.forehapp.store.productModule.application.dto.UpdateProductRequestDto;
import com.forehapp.store.productModule.application.dto.UpdateVariantDto;
import com.forehapp.store.productModule.application.dto.VariantCostHistoryResponse;

import java.util.List;

public interface IProductService {
    ProductResponse createProduct(CreateProductRequestDto dto, Long storeId, Long userId);
    ProductResponse updateProduct(Long productId, UpdateProductRequestDto dto, Long storeId, Long userId);
    ProductVariantResponse addVariant(Long productId, CreateVariantDto dto, Long storeId, Long userId);
    ProductVariantResponse updateVariant(Long productId, Long variantId, UpdateVariantDto dto, Long storeId, Long userId);
    List<VariantCostHistoryResponse> getVariantCostHistory(Long productId, Long variantId, Long storeId, Long userId);
    ProductResponse publish(Long productId, Long storeId, Long userId);
    ProductResponse activate(Long productId, Long storeId, Long userId);
    ProductResponse deactivate(Long productId, Long storeId, Long userId);
    List<ProductResponse> getStoreProducts(Long storeId, Long userId);
    SellerProductDetailResponse getStoreProductById(Long productId, Long storeId, Long userId);
    void deleteProduct(Long productId, Long storeId, Long userId);
    void deleteVariant(Long productId, Long variantId, Long storeId, Long userId);
    ProductVariantResponse deactivateVariant(Long productId, Long variantId, Long storeId, Long userId);
    ProductVariantResponse activateVariant(Long productId, Long variantId, Long storeId, Long userId);
}
