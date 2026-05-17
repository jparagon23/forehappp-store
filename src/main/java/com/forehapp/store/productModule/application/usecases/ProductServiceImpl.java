package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
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
import com.forehapp.store.productModule.domain.ports.out.*;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
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
    private final IStoreProfileDao storeProfileDao;
    private final IProductImageDao productImageDao;
    private final IProductVariantDao variantDao;
    private final IInventoryMovementDao movementDao;
    private final StorageService storageService;

    public ProductServiceImpl(IProductDao productDao,
                              IBrandDao brandDao,
                              ILineDao lineDao,
                              ICategoryDao categoryDao,
                              IAttributeValueDao attributeValueDao,
                              IStoreProfileDao storeProfileDao,
                              IProductImageDao productImageDao,
                              IProductVariantDao variantDao,
                              IInventoryMovementDao movementDao,
                              StorageService storageService) {
        this.productDao = productDao;
        this.brandDao = brandDao;
        this.lineDao = lineDao;
        this.categoryDao = categoryDao;
        this.attributeValueDao = attributeValueDao;
        this.storeProfileDao = storeProfileDao;
        this.productImageDao = productImageDao;
        this.variantDao = variantDao;
        this.movementDao = movementDao;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequestDto dto, Long userId) {
        StoreProfile seller = resolveSeller(userId);

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

        Product product = new Product();
        product.setSeller(seller);
        product.setTitle(dto.getTitle().trim());
        product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        product.setBrand(brand);
        product.setLine(line);
        product.setCategory(category);
        product.setStatus(ProductStatus.DRAFT);

        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, UpdateProductRequestDto dto, Long userId) {
        Product product = resolveOwnedProduct(productId, userId);

        if (dto.getTitle() != null) {
            product.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription().trim());
        }
        if (dto.getBrandId() != null) {
            Brand brand = brandDao.findById(dto.getBrandId())
                    .orElseThrow(() -> new NotFoundException("Brand not found"));
            product.setBrand(brand);
            if (product.getLine() != null && !product.getLine().getBrand().getId().equals(brand.getId())) {
                product.setLine(null);
            }
        }
        if (dto.getLineId() != null) {
            Line line = lineDao.findById(dto.getLineId())
                    .orElseThrow(() -> new NotFoundException("Line not found"));
            if (!line.getBrand().getId().equals(product.getBrand().getId())) {
                throw new BadRequestException("Line does not belong to the product's brand");
            }
            product.setLine(line);
        }

        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    public ProductVariantResponse addVariant(Long productId, CreateVariantDto dto, Long userId) {
        Product product = resolveOwnedProduct(productId, userId);

        if (variantDao.existsBySku(dto.getSku().trim())) {
            throw new BadRequestException("SKU already exists: " + dto.getSku());
        }

        validateCompareAtPrice(dto);

        List<CategoryAttribute> categoryAttrs = categoryDao.findCategoryAttributes(product.getCategory().getId());
        Map<Long, AttributeValue> attrValueMap = fetchAttributeValues(dto.getAttributeValueIds());
        List<AttributeValue> attrValues = resolveAttributeValues(dto.getAttributeValueIds(), attrValueMap);
        validateAttributeValues(dto.getSku(), attrValues, categoryAttrs);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(dto.getSku().trim());
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
    public ProductVariantResponse updateVariant(Long productId, Long variantId, UpdateVariantDto dto, Long userId) {
        resolveOwnedProduct(productId, userId);

        ProductVariant variant = variantDao.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new NotFoundException("Variant not found"));

        if (dto.getPrice() != null) {
            variant.setPrice(dto.getPrice());
        }
        if (dto.isClearCompareAtPrice()) {
            variant.setCompareAtPrice(null);
        } else if (dto.getCompareAtPrice() != null) {
            if (dto.getCompareAtPrice().compareTo(variant.getPrice()) <= 0) {
                throw new BadRequestException("Compare-at price must be greater than sale price");
            }
            variant.setCompareAtPrice(dto.getCompareAtPrice());
        }

        return new ProductVariantResponse(variantDao.save(variant));
    }

    @Override
    @Transactional
    public ProductResponse publish(Long productId, Long userId) {
        Product product = resolveOwnedProduct(productId, userId);

        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is already active");
        }
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            throw new BadRequestException("Product is out of stock and cannot be published");
        }
        if (product.getVariants().isEmpty()) {
            throw new BadRequestException("Product must have at least one variant before publishing");
        }
        if (!productImageDao.existsByProductId(productId)) {
            throw new BadRequestException("Product must have at least one image before publishing");
        }
        boolean hasStock = product.getVariants().stream().anyMatch(v -> v.getStock() > 0);
        if (!hasStock) {
            throw new BadRequestException("Product must have at least one variant with stock before publishing");
        }

        product.setStatus(ProductStatus.ACTIVE);
        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    public ProductResponse deactivate(Long productId, Long userId) {
        Product product = resolveOwnedProduct(productId, userId);

        if (product.getStatus() == ProductStatus.DRAFT) {
            throw new BadRequestException("Product is still a draft — delete it instead");
        }
        if (product.getStatus() == ProductStatus.INACTIVE) {
            throw new BadRequestException("Product is already inactive");
        }

        product.setStatus(ProductStatus.INACTIVE);
        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    public ProductResponse activate(Long productId, Long userId) {
        Product product = resolveOwnedProduct(productId, userId);

        if (product.getStatus() == ProductStatus.DRAFT) {
            throw new BadRequestException("Use the publish endpoint to activate a draft product");
        }
        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is already active");
        }

        product.setStatus(ProductStatus.ACTIVE);
        return new ProductResponse(productDao.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        Product product = resolveOwnedProduct(productId, userId);
        if (product.getStatus() != ProductStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT products can be deleted. Deactivate it instead.");
        }
        productImageDao.findByProductId(productId)
                .forEach(img -> storageService.delete(img.getS3Key()));
        productImageDao.deleteAllByProductId(productId);
        productDao.delete(product);
    }

    @Override
    @Transactional
    public void deleteVariant(Long productId, Long variantId, Long userId) {
        Product product = resolveOwnedProduct(productId, userId);
        ProductVariant variant = variantDao.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new NotFoundException("Variant not found"));

        if (product.getVariants().size() == 1) {
            throw new BadRequestException("Cannot delete the last variant. Delete the product instead.");
        }

        variantDao.delete(variant);

        boolean hasStock = product.getVariants().stream()
                .filter(v -> !v.getId().equals(variantId))
                .anyMatch(v -> v.getStock() > 0);
        if (!hasStock && product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
            productDao.save(product);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SellerProductDetailResponse getSellerProductById(Long productId, Long userId) {
        Product product = resolveOwnedProduct(productId, userId);
        List<ProductImageResponse> images = productImageDao.findByProductId(productId).stream()
                .map(img -> new ProductImageResponse(img, storageService.presign(img.getS3Key(), Duration.ofDays(7))))
                .toList();
        return new SellerProductDetailResponse(product, images);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getSellerProducts(Long userId) {
        StoreProfile seller = resolveSeller(userId);
        return productDao.findAllBySellerId(seller.getId()).stream()
                .map(ProductResponse::new)
                .toList();
    }

    private StoreProfile resolveSeller(Long userId) {
        StoreProfile seller = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Store profile not found"));
        if (!seller.getRoles().contains(StoreRole.SELLER)) {
            throw new BadRequestException("User does not have SELLER role");
        }
        return seller;
    }

    private Product resolveOwnedProduct(Long productId, Long userId) {
        StoreProfile seller = resolveSeller(userId);
        return productDao.findByIdAndSellerId(productId, seller.getId())
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private void validateCompareAtPrice(CreateVariantDto dto) {
        if (dto.getCompareAtPrice() != null
                && dto.getCompareAtPrice().compareTo(dto.getPrice()) <= 0) {
            throw new BadRequestException(
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
                    if (av == null) throw new NotFoundException("Attribute value not found: " + id);
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
                throw new BadRequestException(
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
                    throw new BadRequestException(
                            "Required attribute missing in variant '" + sku + "': " + attrName);
                });
    }
}
