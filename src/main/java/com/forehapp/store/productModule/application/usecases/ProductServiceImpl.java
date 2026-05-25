package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.application.dto.CreateProductRequestDto;
import com.forehapp.store.productModule.application.dto.CreateVariantDto;
import com.forehapp.store.productModule.application.dto.ProductImageResponse;
import com.forehapp.store.productModule.application.dto.ProductResponse;
import com.forehapp.store.productModule.application.dto.ProductVariantResponse;
import com.forehapp.store.productModule.application.dto.SellerProductDetailResponse;
import com.forehapp.store.productModule.application.dto.UpdateProductRequestDto;
import com.forehapp.store.productModule.application.dto.UpdateVariantDto;
import com.forehapp.store.productModule.domain.model.*;
import com.forehapp.store.general.storage.StorageService;
import com.forehapp.store.productModule.domain.ports.in.IProductService;
import com.forehapp.store.orderModule.domain.ports.out.IOrderItemDao;
import com.forehapp.store.productModule.domain.ports.out.*;
import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.model.StoreMembership;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    private final IProductDao productDao;
    private final IBrandDao brandDao;
    private final ILineDao lineDao;
    private final ICategoryDao categoryDao;
    private final IAttributeValueDao attributeValueDao;
    private final IStoreMembershipDao membershipDao;
    private final IProductImageDao productImageDao;
    private final IProductVariantDao variantDao;
    private final IInventoryMovementDao movementDao;
    private final IOrderItemDao orderItemDao;
    private final StorageService storageService;

    public ProductServiceImpl(IProductDao productDao,
                              IBrandDao brandDao,
                              ILineDao lineDao,
                              ICategoryDao categoryDao,
                              IAttributeValueDao attributeValueDao,
                              IStoreMembershipDao membershipDao,
                              IProductImageDao productImageDao,
                              IProductVariantDao variantDao,
                              IInventoryMovementDao movementDao,
                              IOrderItemDao orderItemDao,
                              StorageService storageService) {
        this.productDao = productDao;
        this.brandDao = brandDao;
        this.lineDao = lineDao;
        this.categoryDao = categoryDao;
        this.attributeValueDao = attributeValueDao;
        this.membershipDao = membershipDao;
        this.productImageDao = productImageDao;
        this.variantDao = variantDao;
        this.movementDao = movementDao;
        this.orderItemDao = orderItemDao;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequestDto dto, Long storeId, Long userId) {
        Store store = resolveStoreAccess(storeId, userId).getStore();

        Brand brand = brandDao.findById(dto.getBrandId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Brand not found"));

        Line line = null;
        if (dto.getLineId() != null) {
            line = lineDao.findById(dto.getLineId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Line not found"));
            if (!line.getBrand().getId().equals(brand.getId())) {
                throw new BadRequestException(ErrorCode.PRODUCT_LINE_BRAND_MISMATCH, "Line does not belong to the specified brand");
            }
        }

        Category category = categoryDao.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Category not found"));

        Product product = new Product();
        product.setStore(store);
        product.setTitle(dto.getTitle().trim());
        product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        product.setBrand(brand);
        product.setLine(line);
        product.setCategory(category);
        product.setStatus(ProductStatus.DRAFT);
        product.setFreeShipping(Boolean.TRUE.equals(dto.getFreeShipping()));

        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public ProductResponse updateProduct(Long productId, UpdateProductRequestDto dto, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);

        if (dto.getTitle() != null) {
            product.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription().trim());
        }
        if (dto.getBrandId() != null) {
            Brand brand = brandDao.findById(dto.getBrandId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Brand not found"));
            product.setBrand(brand);
            if (product.getLine() != null && !product.getLine().getBrand().getId().equals(brand.getId())) {
                product.setLine(null);
            }
        }
        if (dto.getLineId() != null) {
            Line line = lineDao.findById(dto.getLineId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Line not found"));
            if (!line.getBrand().getId().equals(product.getBrand().getId())) {
                throw new BadRequestException(ErrorCode.PRODUCT_LINE_BRAND_MISMATCH, "Line does not belong to the product's brand");
            }
            product.setLine(line);
        }
        if (dto.getFreeShipping() != null) {
            product.setFreeShipping(dto.getFreeShipping());
        }

        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public ProductVariantResponse addVariant(Long productId, CreateVariantDto dto, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);

        if (dto.getSku() != null && !dto.getSku().isBlank() && variantDao.existsBySku(dto.getSku().trim())) {
            throw new BadRequestException(ErrorCode.PRODUCT_SKU_DUPLICATE, "SKU already exists: " + dto.getSku());
        }

        validateCompareAtPrice(dto);

        List<CategoryAttribute> categoryAttrs = categoryDao.findCategoryAttributes(product.getCategory().getId());
        Map<Long, AttributeValue> attrValueMap = fetchAttributeValues(dto.getAttributeValueIds());
        List<AttributeValue> attrValues = resolveAttributeValues(dto.getAttributeValueIds(), attrValueMap);
        validateAttributeValues(dto.getSku(), attrValues, categoryAttrs);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(dto.getSku() != null && !dto.getSku().isBlank() ? dto.getSku().trim() : null);
        variant.setPrice(dto.getPrice());
        variant.setCompareAtPrice(dto.getCompareAtPrice());
        variant.setStock(dto.getStock());
        variant.setAttributeValues(attrValues);

        ProductVariant saved = variantDao.save(variant);

        if (dto.getStock() > 0) {
            InventoryMovement movement = new InventoryMovement();
            movement.setVariant(saved);
            movement.setQuantity(dto.getStock());
            movement.setReason(MovementReason.RESTOCK);
            movementDao.save(movement);
        }

        return new ProductVariantResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public ProductVariantResponse updateVariant(Long productId, Long variantId, UpdateVariantDto dto, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        resolveStoreProduct(productId, storeId);

        ProductVariant variant = variantDao.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Variant not found"));

        if (dto.getPrice() != null) {
            variant.setPrice(dto.getPrice());
        }
        if (dto.isClearCompareAtPrice()) {
            variant.setCompareAtPrice(null);
        } else if (dto.getCompareAtPrice() != null) {
            if (dto.getCompareAtPrice().compareTo(variant.getPrice()) <= 0) {
                throw new BadRequestException(ErrorCode.PRODUCT_COMPARE_PRICE_INVALID, "Compare-at price must be greater than sale price");
            }
            variant.setCompareAtPrice(dto.getCompareAtPrice());
        }

        return new ProductVariantResponse(variantDao.save(variant));
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public ProductResponse publish(Long productId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);

        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new BadRequestException(ErrorCode.PRODUCT_ALREADY_ACTIVE, "Product is already active");
        }
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            throw new BadRequestException(ErrorCode.PRODUCT_OUT_OF_STOCK, "Product is out of stock and cannot be published");
        }
        if (product.getVariants().isEmpty()) {
            throw new BadRequestException(ErrorCode.PRODUCT_NO_VARIANTS, "Product must have at least one variant before publishing");
        }
        if (!productImageDao.existsByProductId(productId)) {
            throw new BadRequestException(ErrorCode.PRODUCT_NO_IMAGES, "Product must have at least one image before publishing");
        }
        boolean hasStock = product.getVariants().stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getActive()) && v.getStock() > 0);
        if (!hasStock) {
            throw new BadRequestException(ErrorCode.PRODUCT_NO_STOCK_IN_VARIANTS, "Product must have at least one active variant with stock before publishing");
        }

        product.setStatus(ProductStatus.ACTIVE);
        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public ProductResponse deactivate(Long productId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);

        if (product.getStatus() == ProductStatus.DRAFT) {
            throw new BadRequestException(ErrorCode.PRODUCT_NOT_DRAFT, "Product is still a draft — delete it instead");
        }
        if (product.getStatus() == ProductStatus.INACTIVE) {
            throw new BadRequestException(ErrorCode.PRODUCT_ALREADY_INACTIVE, "Product is already inactive");
        }

        product.setStatus(ProductStatus.INACTIVE);
        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public ProductResponse activate(Long productId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);

        if (product.getStatus() == ProductStatus.DRAFT) {
            throw new BadRequestException(ErrorCode.PRODUCT_USE_PUBLISH_ENDPOINT, "Use the publish endpoint to activate a draft product");
        }
        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new BadRequestException(ErrorCode.PRODUCT_ALREADY_ACTIVE, "Product is already active");
        }

        product.setStatus(ProductStatus.ACTIVE);
        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public void deleteProduct(Long productId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);
        if (product.getStatus() != ProductStatus.DRAFT) {
            throw new BadRequestException(ErrorCode.PRODUCT_DELETE_REQUIRES_DRAFT, "Only DRAFT products can be deleted. Deactivate it instead.");
        }
        productImageDao.findByProductId(productId)
                .forEach(img -> storageService.delete(img.getS3Key()));
        productImageDao.deleteAllByProductId(productId);
        productDao.delete(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public void deleteVariant(Long productId, Long variantId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);

        boolean existed = product.getVariants().stream().anyMatch(v -> v.getId().equals(variantId));
        if (!existed) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Variant not found");
        }
        if (product.getVariants().size() == 1) {
            throw new BadRequestException(ErrorCode.PRODUCT_LAST_VARIANT, "Cannot delete the last variant. Delete the product instead.");
        }
        if (orderItemDao.existsByVariantId(variantId)) {
            throw new ConflictException(ErrorCode.PRODUCT_VARIANT_HAS_ORDERS, "This variant has associated orders and cannot be deleted. Deactivate it instead.");
        }

        product.getVariants().removeIf(v -> v.getId().equals(variantId));

        boolean hasStock = product.getVariants().stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getActive()) && v.getStock() > 0);
        if (!hasStock && product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }

        productDao.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerProductDetailResponse getStoreProductById(Long productId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);
        List<ProductImageResponse> images = productImageDao.findByProductId(productId).stream()
                .map(img -> new ProductImageResponse(img, storageService.presign(img.getS3Key(), Duration.ofDays(7))))
                .toList();
        return new SellerProductDetailResponse(product, images);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getStoreProducts(Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        return productDao.findAllByStoreId(storeId).stream()
                .map(p -> {
                    String thumbnail = p.getImages().stream()
                            .findFirst()
                            .map(img -> storageService.presign(img.getS3Key(), Duration.ofDays(7)))
                            .filter(url -> !url.isBlank())
                            .orElse(null);
                    return new ProductResponse(p, thumbnail);
                })
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public ProductVariantResponse deactivateVariant(Long productId, Long variantId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);
        ProductVariant variant = variantDao.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Variant not found"));

        if (!Boolean.TRUE.equals(variant.getActive())) {
            throw new BadRequestException(ErrorCode.PRODUCT_VARIANT_ALREADY_INACTIVE, "Variant is already inactive");
        }

        long activeCount = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .count();
        if (activeCount <= 1) {
            throw new BadRequestException(ErrorCode.PRODUCT_VARIANT_LAST_ACTIVE,
                    "Cannot deactivate the last active variant. Deactivate the product instead.");
        }

        variant.setActive(false);
        ProductVariant saved = variantDao.save(variant);

        boolean hasStock = product.getVariants().stream()
                .filter(v -> !v.getId().equals(variantId))
                .anyMatch(v -> Boolean.TRUE.equals(v.getActive()) && v.getStock() > 0);
        if (!hasStock && product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
            productDao.save(product);
        }

        return new ProductVariantResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public ProductVariantResponse activateVariant(Long productId, Long variantId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        resolveStoreProduct(productId, storeId);
        ProductVariant variant = variantDao.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Variant not found"));

        if (Boolean.TRUE.equals(variant.getActive())) {
            throw new BadRequestException(ErrorCode.PRODUCT_VARIANT_ALREADY_ACTIVE, "Variant is already active");
        }

        variant.setActive(true);
        return new ProductVariantResponse(variantDao.save(variant));
    }

    private StoreMembership resolveStoreAccess(Long storeId, Long userId) {
        return membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .filter(m -> m.getRole() != StoreMemberRole.STAFF)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED, "You do not have permission to manage this store's products"));
    }

    private Product resolveStoreProduct(Long productId, Long storeId) {
        return productDao.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found"));
    }

    private void validateCompareAtPrice(CreateVariantDto dto) {
        if (dto.getCompareAtPrice() != null
                && dto.getCompareAtPrice().compareTo(dto.getPrice()) <= 0) {
            throw new BadRequestException(ErrorCode.PRODUCT_COMPARE_PRICE_INVALID,
                    "Compare-at price must be greater than sale price for SKU: " + dto.getSku());
        }
    }

    private Map<Long, AttributeValue> fetchAttributeValues(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return attributeValueDao.findAllByIds(ids).stream()
                .collect(Collectors.toMap(AttributeValue::getId, av -> av));
    }

    private List<AttributeValue> resolveAttributeValues(List<Long> ids,
                                                        Map<Long, AttributeValue> attrValueMap) {
        if (ids == null) return List.of();
        return ids.stream()
                .map(id -> {
                    AttributeValue av = attrValueMap.get(id);
                    if (av == null) throw new NotFoundException(ErrorCode.PRODUCT_ATTRIBUTE_VALUE_NOT_FOUND, "Attribute value not found: " + id);
                    return av;
                })
                .toList();
    }

    private void validateAttributeValues(String sku,
                                         List<AttributeValue> attrValues,
                                         List<CategoryAttribute> categoryAttrs) {
        if (categoryAttrs.isEmpty()) return;

        Set<Long> allowedIds = categoryAttrs.stream()
                .map(ca -> ca.getAttribute().getId())
                .collect(Collectors.toSet());

        Set<Long> requiredIds = categoryAttrs.stream()
                .filter(ca -> "T".equals(ca.getRequired()))
                .map(ca -> ca.getAttribute().getId())
                .collect(Collectors.toSet());

        for (AttributeValue av : attrValues) {
            if (!allowedIds.contains(av.getAttribute().getId())) {
                throw new BadRequestException(ErrorCode.PRODUCT_ATTRIBUTE_VALUE_NOT_FOUND,
                        "Attribute '" + av.getAttribute().getDescription() + "' does not apply to this category");
            }
        }

        Set<Long> providedIds = attrValues.stream()
                .map(av -> av.getAttribute().getId())
                .collect(Collectors.toSet());

        requiredIds.stream()
                .filter(reqId -> !providedIds.contains(reqId))
                .findFirst()
                .ifPresent(missingId -> {
                    String attrName = categoryAttrs.stream()
                            .filter(ca -> ca.getAttribute().getId().equals(missingId))
                            .map(ca -> ca.getAttribute().getDescription())
                            .findFirst().orElse("unknown");
                    throw new BadRequestException(ErrorCode.PRODUCT_ATTRIBUTE_VALUE_NOT_FOUND,
                            "Required attribute missing in variant '" + sku + "': " + attrName);
                });
    }
}
