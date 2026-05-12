package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.BrandResponse;
import com.forehapp.store.productModule.application.dto.CategoryAttributeResponse;
import com.forehapp.store.productModule.application.dto.CategoryResponse;
import com.forehapp.store.productModule.application.dto.LineResponse;

import java.util.List;

public interface ICatalogService {
    List<BrandResponse> findAllBrands();
    List<LineResponse> findLinesByBrand(Long brandId);
    List<CategoryResponse> findAllCategories();
    List<CategoryAttributeResponse> findCategoryAttributes(Long categoryId);
}
