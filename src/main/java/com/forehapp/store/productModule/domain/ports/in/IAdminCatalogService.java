package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.*;

public interface IAdminCatalogService {
    BrandResponse createBrand(CreateBrandRequestDto dto, Long userId);
    LineResponse createLine(Long brandId, CreateLineRequestDto dto, Long userId);
    CategoryResponse createCategory(CreateCategoryRequestDto dto, Long userId);
    AttributeResponse createAttribute(CreateAttributeRequestDto dto, Long userId);
    CategoryAttributeResponse.AttributeValueDto createAttributeValue(Long attributeId, CreateAttributeValueRequestDto dto, Long userId);
    CategoryAttributeResponse linkAttributeToCategory(Long categoryId, LinkCategoryAttributeRequestDto dto, Long userId);
}
