package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.*;

import java.util.List;

public interface IAdminCatalogService {

    // Brand
    BrandResponse createBrand(CreateBrandRequestDto dto, Long userId);
    BrandResponse updateBrand(Long brandId, CreateBrandRequestDto dto, Long userId);
    void deleteBrand(Long brandId, Long userId);

    // Line
    LineResponse createLine(Long brandId, CreateLineRequestDto dto, Long userId);
    LineResponse updateLine(Long brandId, Long lineId, UpdateLineRequestDto dto, Long userId);
    void deleteLine(Long brandId, Long lineId, Long userId);

    // Category
    List<CategoryResponse> listCategories(Long userId);
    CategoryResponse createCategory(CreateCategoryRequestDto dto, Long userId);
    CategoryResponse updateCategory(Long categoryId, CreateCategoryRequestDto dto, Long userId);
    CategoryResponse updateCategoryDiscoveryOrder(Long categoryId, UpdateCategoryDiscoveryOrderRequestDto dto, Long userId);
    void deleteCategory(Long categoryId, Long userId);

    // Attribute
    AttributeResponse createAttribute(CreateAttributeRequestDto dto, Long userId);
    AttributeResponse updateAttribute(Long attributeId, CreateAttributeRequestDto dto, Long userId);
    void deleteAttribute(Long attributeId, Long userId);

    // AttributeValue
    CategoryAttributeResponse.AttributeValueDto createAttributeValue(Long attributeId, CreateAttributeValueRequestDto dto, Long userId);
    CategoryAttributeResponse.AttributeValueDto updateAttributeValue(Long attributeId, Long valueId, CreateAttributeValueRequestDto dto, Long userId);
    void deleteAttributeValue(Long attributeId, Long valueId, Long userId);

    // CategoryAttribute link
    CategoryAttributeResponse linkAttributeToCategory(Long categoryId, LinkCategoryAttributeRequestDto dto, Long userId);
    CategoryAttributeResponse updateCategoryAttribute(Long categoryId, Long attributeId, UpdateCategoryAttributeRequestDto dto, Long userId);
    void unlinkAttributeFromCategory(Long categoryId, Long attributeId, Long userId);
}
