package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.AttributeResponse;
import com.forehapp.store.productModule.application.dto.BrandResponse;
import com.forehapp.store.productModule.application.dto.CategoryAttributeResponse;
import com.forehapp.store.productModule.application.dto.CategoryResponse;
import com.forehapp.store.productModule.application.dto.LineResponse;

import java.util.List;

public interface ICatalogService {
    List<BrandResponse> findAllBrands();
    List<LineResponse> findLinesByBrand(Long brandId);
    List<LineResponse> findLinesByBrandAndCategory(Long brandId, Long categoryId);
    List<CategoryResponse> findAllCategories(boolean hasProducts);
    List<CategoryAttributeResponse> findCategoryAttributes(Long categoryId);
    List<AttributeResponse> findAllAttributes();
    List<CategoryAttributeResponse.AttributeValueDto> findAttributeValues(Long attributeId);
}
