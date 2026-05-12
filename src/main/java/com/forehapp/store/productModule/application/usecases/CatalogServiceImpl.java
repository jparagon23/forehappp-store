package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.application.dto.BrandResponse;
import com.forehapp.store.productModule.application.dto.CategoryAttributeResponse;
import com.forehapp.store.productModule.application.dto.CategoryResponse;
import com.forehapp.store.productModule.application.dto.LineResponse;
import com.forehapp.store.productModule.domain.model.AttributeValue;
import com.forehapp.store.productModule.domain.model.CategoryAttribute;
import com.forehapp.store.productModule.domain.ports.in.ICatalogService;
import com.forehapp.store.productModule.domain.ports.out.IAttributeValueDao;
import com.forehapp.store.productModule.domain.ports.out.IBrandDao;
import com.forehapp.store.productModule.domain.ports.out.ICategoryDao;
import com.forehapp.store.productModule.domain.ports.out.ILineDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CatalogServiceImpl implements ICatalogService {

    private final IBrandDao brandDao;
    private final ILineDao lineDao;
    private final ICategoryDao categoryDao;
    private final IAttributeValueDao attributeValueDao;

    public CatalogServiceImpl(IBrandDao brandDao,
                              ILineDao lineDao,
                              ICategoryDao categoryDao,
                              IAttributeValueDao attributeValueDao) {
        this.brandDao = brandDao;
        this.lineDao = lineDao;
        this.categoryDao = categoryDao;
        this.attributeValueDao = attributeValueDao;
    }

    @Override
    public List<BrandResponse> findAllBrands() {
        return brandDao.findAll().stream()
                .map(BrandResponse::new)
                .toList();
    }

    @Override
    public List<LineResponse> findLinesByBrand(Long brandId) {
        brandDao.findById(brandId)
                .orElseThrow(() -> new NotFoundException("Brand not found"));
        return lineDao.findAllByBrandId(brandId).stream()
                .map(LineResponse::new)
                .toList();
    }

    @Override
    public List<CategoryResponse> findAllCategories() {
        return categoryDao.findAll().stream()
                .map(CategoryResponse::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryAttributeResponse> findCategoryAttributes(Long categoryId) {
        categoryDao.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        List<CategoryAttribute> categoryAttrs = categoryDao.findCategoryAttributes(categoryId);
        if (categoryAttrs.isEmpty()) return List.of();

        List<Long> attributeIds = categoryAttrs.stream()
                .map(ca -> ca.getAttribute().getId())
                .toList();

        Map<Long, List<AttributeValue>> valuesByAttributeId = attributeValueDao
                .findAllByAttributeIds(attributeIds)
                .stream()
                .collect(Collectors.groupingBy(av -> av.getAttribute().getId()));

        return categoryAttrs.stream()
                .map(ca -> new CategoryAttributeResponse(
                        ca.getAttribute().getId(),
                        ca.getAttribute().getDescription(),
                        "T".equals(ca.getRequired()),
                        valuesByAttributeId.getOrDefault(ca.getAttribute().getId(), List.of())
                                .stream()
                                .map(av -> new CategoryAttributeResponse.AttributeValueDto(
                                        av.getId(), av.getDescription()))
                                .toList()
                ))
                .toList();
    }
}
