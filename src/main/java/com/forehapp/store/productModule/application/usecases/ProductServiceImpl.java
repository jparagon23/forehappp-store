package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.application.dto.CreateProductRequestDto;
import com.forehapp.store.productModule.application.dto.CreateVariantDto;
import com.forehapp.store.productModule.application.dto.ProductResponse;
import com.forehapp.store.productModule.domain.model.*;
import com.forehapp.store.productModule.domain.ports.in.IProductService;
import com.forehapp.store.productModule.domain.ports.out.*;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    private final IProductDao productDao;
    private final IBrandDao brandDao;
    private final ILineDao lineDao;
    private final ICategoryDao categoryDao;
    private final IAttributeValueDao attributeValueDao;
    private final IStoreProfileDao storeProfileDao;

    public ProductServiceImpl(IProductDao productDao,
                              IBrandDao brandDao,
                              ILineDao lineDao,
                              ICategoryDao categoryDao,
                              IAttributeValueDao attributeValueDao,
                              IStoreProfileDao storeProfileDao) {
        this.productDao = productDao;
        this.brandDao = brandDao;
        this.lineDao = lineDao;
        this.categoryDao = categoryDao;
        this.attributeValueDao = attributeValueDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequestDto dto, Long userId) {
        StoreProfile seller = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Store profile not found"));

        if (!seller.getRoles().contains(StoreRole.SELLER)) {
            throw new BadRequestException("User does not have SELLER role");
        }

        Brand brand = brandDao.findById(dto.getBrandId())
                .orElseThrow(() -> new NotFoundException("Brand not found"));

        Line line = null;
        if (dto.getLineId() != null) {
            line = lineDao.findById(dto.getLineId())
                    .orElseThrow(() -> new NotFoundException("Line not found"));
            if (!line.getBrand().getId().equals(brand.getId())) {
                throw new BadRequestException("Line does not belong to the specified brand");
            }
        }

        Category category = categoryDao.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        validateNoDuplicateSkus(dto.getVariants());

        List<CategoryAttribute> categoryAttrs = categoryDao.findCategoryAttributes(category.getId());
        Set<Long> allowedAttributeIds = categoryAttrs.stream()
                .map(ca -> ca.getAttribute().getId())
                .collect(Collectors.toSet());
        Set<Long> requiredAttributeIds = categoryAttrs.stream()
                .filter(ca -> "T".equals(ca.getRequired()))
                .map(ca -> ca.getAttribute().getId())
                .collect(Collectors.toSet());

        Map<Long, AttributeValue> attrValueMap = fetchAttributeValues(dto.getVariants());

        Product product = new Product();
        product.setSeller(seller);
        product.setDescription(dto.getDescription().trim());
        product.setBrand(brand);
        product.setLine(line);
        product.setCategory(category);
        product.setStatus(ProductStatus.ACTIVE);

        for (CreateVariantDto variantDto : dto.getVariants()) {
            List<AttributeValue> attrValues = resolveAttributeValues(variantDto, attrValueMap);
            validateAttributeValues(variantDto.getSku(), attrValues, allowedAttributeIds, requiredAttributeIds, categoryAttrs);

            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(variantDto.getSku().trim());
            variant.setPrice(variantDto.getPrice());
            variant.setStock(variantDto.getStock());
            variant.setAttributeValues(attrValues);

            product.getVariants().add(variant);
        }

        return new ProductResponse(productDao.save(product));
    }

    private void validateNoDuplicateSkus(List<CreateVariantDto> variants) {
        Set<String> seen = new HashSet<>();
        for (CreateVariantDto v : variants) {
            if (!seen.add(v.getSku().trim())) {
                throw new BadRequestException("Duplicate SKU in request: " + v.getSku());
            }
        }
    }

    private Map<Long, AttributeValue> fetchAttributeValues(List<CreateVariantDto> variants) {
        List<Long> ids = variants.stream()
                .flatMap(v -> v.getAttributeValueIds().stream())
                .distinct()
                .toList();
        if (ids.isEmpty()) return Map.of();
        return attributeValueDao.findAllByIds(ids).stream()
                .collect(Collectors.toMap(AttributeValue::getId, av -> av));
    }

    private List<AttributeValue> resolveAttributeValues(CreateVariantDto variantDto,
                                                        Map<Long, AttributeValue> attrValueMap) {
        return variantDto.getAttributeValueIds().stream()
                .map(id -> {
                    AttributeValue av = attrValueMap.get(id);
                    if (av == null) throw new NotFoundException("Attribute value not found: " + id);
                    return av;
                })
                .toList();
    }

    private void validateAttributeValues(String sku,
                                         List<AttributeValue> attrValues,
                                         Set<Long> allowedAttributeIds,
                                         Set<Long> requiredAttributeIds,
                                         List<CategoryAttribute> categoryAttrs) {
        if (allowedAttributeIds.isEmpty()) return;

        for (AttributeValue av : attrValues) {
            if (!allowedAttributeIds.contains(av.getAttribute().getId())) {
                throw new BadRequestException(
                        "Attribute '" + av.getAttribute().getDescription() + "' does not apply to the selected category");
            }
        }

        Set<Long> variantAttrIds = attrValues.stream()
                .map(av -> av.getAttribute().getId())
                .collect(Collectors.toSet());

        requiredAttributeIds.stream()
                .filter(reqId -> !variantAttrIds.contains(reqId))
                .findFirst()
                .ifPresent(missingId -> {
                    String attrName = categoryAttrs.stream()
                            .filter(ca -> ca.getAttribute().getId().equals(missingId))
                            .map(ca -> ca.getAttribute().getDescription())
                            .findFirst().orElse("unknown");
                    throw new BadRequestException(
                            "Required attribute missing in variant '" + sku + "': " + attrName);
                });
    }
}
