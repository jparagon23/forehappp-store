package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.application.dto.*;
import com.forehapp.store.productModule.domain.model.*;
import com.forehapp.store.productModule.domain.ports.in.IAdminCatalogService;
import com.forehapp.store.productModule.domain.ports.out.*;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminCatalogServiceImpl implements IAdminCatalogService {

    private final IBrandDao brandDao;
    private final ILineDao lineDao;
    private final ICategoryDao categoryDao;
    private final IAttributeDao attributeDao;
    private final IAttributeValueDao attributeValueDao;
    private final IStoreProfileDao storeProfileDao;

    public AdminCatalogServiceImpl(IBrandDao brandDao,
                                   ILineDao lineDao,
                                   ICategoryDao categoryDao,
                                   IAttributeDao attributeDao,
                                   IAttributeValueDao attributeValueDao,
                                   IStoreProfileDao storeProfileDao) {
        this.brandDao = brandDao;
        this.lineDao = lineDao;
        this.categoryDao = categoryDao;
        this.attributeDao = attributeDao;
        this.attributeValueDao = attributeValueDao;
        this.storeProfileDao = storeProfileDao;
    }

    // ── Brand ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BrandResponse createBrand(CreateBrandRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Brand brand = new Brand();
        brand.setDescription(dto.getName().trim());
        return new BrandResponse(brandDao.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(Long brandId, CreateBrandRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Brand brand = brandDao.findById(brandId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Brand not found"));
        brand.setDescription(dto.getName().trim());
        return new BrandResponse(brandDao.save(brand));
    }

    @Override
    @Transactional
    public void deleteBrand(Long brandId, Long userId) {
        resolveAdmin(userId);
        Brand brand = brandDao.findById(brandId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Brand not found"));
        if (brandDao.isUsedByProducts(brandId)) {
            throw new ConflictException(ErrorCode.CATALOG_BRAND_IN_USE, "Brand is used by one or more products and cannot be deleted");
        }
        brandDao.delete(brand);
    }

    // ── Line ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LineResponse createLine(Long brandId, CreateLineRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Brand brand = brandDao.findById(brandId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Brand not found"));
        Category category = categoryDao.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Category not found"));
        Line line = new Line();
        line.setBrand(brand);
        line.setCategory(category);
        line.setDescription(dto.getName().trim());
        return new LineResponse(lineDao.save(line));
    }

    @Override
    @Transactional
    public LineResponse updateLine(Long brandId, Long lineId, UpdateLineRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Line line = lineDao.findById(lineId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Line not found"));
        if (!line.getBrand().getId().equals(brandId)) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Line not found for this brand");
        }
        line.setDescription(dto.getName().trim());
        return new LineResponse(lineDao.save(line));
    }

    @Override
    @Transactional
    public void deleteLine(Long brandId, Long lineId, Long userId) {
        resolveAdmin(userId);
        Line line = lineDao.findById(lineId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Line not found"));
        if (!line.getBrand().getId().equals(brandId)) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Line not found for this brand");
        }
        if (lineDao.isUsedByProducts(lineId)) {
            throw new ConflictException(ErrorCode.CATALOG_LINE_IN_USE, "Line is used by one or more products and cannot be deleted");
        }
        lineDao.delete(line);
    }

    // ── Category ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(Long userId) {
        resolveAdmin(userId);
        return categoryDao.findAll().stream()
                .map(CategoryResponse::new)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Category category = new Category();
        category.setDescription(dto.getName().trim());
        return new CategoryResponse(categoryDao.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CreateCategoryRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Category category = categoryDao.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Category not found"));
        category.setDescription(dto.getName().trim());
        return new CategoryResponse(categoryDao.save(category));
    }

    @Override
    @Transactional
    @CacheEvict(value = "discovery-sections", allEntries = true)
    public CategoryResponse updateCategoryDiscoveryOrder(Long categoryId, UpdateCategoryDiscoveryOrderRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Category category = categoryDao.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Category not found"));
        category.setSortOrder(dto.getSortOrder());
        return new CategoryResponse(categoryDao.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId, Long userId) {
        resolveAdmin(userId);
        Category category = categoryDao.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Category not found"));
        if (categoryDao.isUsedByProducts(categoryId)) {
            throw new ConflictException(ErrorCode.CATALOG_CATEGORY_IN_USE, "Category is used by one or more products and cannot be deleted");
        }
        categoryDao.delete(category);
    }

    // ── Attribute ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AttributeResponse createAttribute(CreateAttributeRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Attribute attribute = new Attribute();
        attribute.setDescription(dto.getName().trim());
        return new AttributeResponse(attributeDao.save(attribute));
    }

    @Override
    @Transactional
    public AttributeResponse updateAttribute(Long attributeId, CreateAttributeRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Attribute attribute = attributeDao.findById(attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Attribute not found"));
        attribute.setDescription(dto.getName().trim());
        return new AttributeResponse(attributeDao.save(attribute));
    }

    @Override
    @Transactional
    public void deleteAttribute(Long attributeId, Long userId) {
        resolveAdmin(userId);
        Attribute attribute = attributeDao.findById(attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Attribute not found"));
        if (attributeDao.hasValues(attributeId)) {
            throw new ConflictException(ErrorCode.CATALOG_ATTRIBUTE_IN_USE, "Attribute has values; delete all values before deleting the attribute");
        }
        attributeDao.delete(attribute);
    }

    // ── AttributeValue ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CategoryAttributeResponse.AttributeValueDto createAttributeValue(
            Long attributeId, CreateAttributeValueRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Attribute attribute = attributeDao.findById(attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Attribute not found"));
        AttributeValue value = new AttributeValue();
        value.setAttribute(attribute);
        value.setDescription(dto.getDescription().trim());
        AttributeValue saved = attributeValueDao.save(value);
        return new CategoryAttributeResponse.AttributeValueDto(saved.getId(), saved.getDescription());
    }

    @Override
    @Transactional
    public CategoryAttributeResponse.AttributeValueDto updateAttributeValue(
            Long attributeId, Long valueId, CreateAttributeValueRequestDto dto, Long userId) {
        resolveAdmin(userId);
        attributeDao.findById(attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Attribute not found"));
        AttributeValue value = attributeValueDao.findByIdAndAttributeId(valueId, attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Attribute value not found"));
        value.setDescription(dto.getDescription().trim());
        AttributeValue saved = attributeValueDao.save(value);
        return new CategoryAttributeResponse.AttributeValueDto(saved.getId(), saved.getDescription());
    }

    @Override
    @Transactional
    public void deleteAttributeValue(Long attributeId, Long valueId, Long userId) {
        resolveAdmin(userId);
        attributeDao.findById(attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Attribute not found"));
        AttributeValue value = attributeValueDao.findByIdAndAttributeId(valueId, attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Attribute value not found"));
        if (attributeValueDao.isUsedByVariants(valueId)) {
            throw new ConflictException(ErrorCode.CATALOG_ATTRIBUTE_VALUE_IN_USE, "Attribute value is used by one or more product variants and cannot be deleted");
        }
        attributeValueDao.delete(value);
    }

    // ── CategoryAttribute link ────────────────────────────────────────────────

    @Override
    @Transactional
    public CategoryAttributeResponse linkAttributeToCategory(
            Long categoryId, LinkCategoryAttributeRequestDto dto, Long userId) {
        resolveAdmin(userId);

        Category category = categoryDao.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Category not found"));
        Attribute attribute = attributeDao.findById(dto.getAttributeId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Attribute not found"));

        if (categoryDao.existsCategoryAttribute(categoryId, dto.getAttributeId())) {
            throw new BadRequestException(ErrorCode.PRODUCT_ATTRIBUTE_ALREADY_LINKED, "Attribute is already linked to this category");
        }

        CategoryAttribute ca = new CategoryAttribute();
        ca.setCategory(category);
        ca.setAttribute(attribute);
        ca.setRequired(dto.isRequired() ? "T" : "F");
        categoryDao.saveCategoryAttribute(ca);

        return new CategoryAttributeResponse(
                attribute.getId(),
                attribute.getDescription(),
                dto.isRequired(),
                List.of()
        );
    }

    @Override
    @Transactional
    public CategoryAttributeResponse updateCategoryAttribute(
            Long categoryId, Long attributeId, UpdateCategoryAttributeRequestDto dto, Long userId) {
        resolveAdmin(userId);
        CategoryAttribute ca = categoryDao.findCategoryAttribute(categoryId, attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Category-attribute link not found"));
        ca.setRequired(dto.isRequired() ? "T" : "F");
        CategoryAttribute saved = categoryDao.saveCategoryAttribute(ca);
        return new CategoryAttributeResponse(
                saved.getAttribute().getId(),
                saved.getAttribute().getDescription(),
                dto.isRequired(),
                List.of()
        );
    }

    @Override
    @Transactional
    public void unlinkAttributeFromCategory(Long categoryId, Long attributeId, Long userId) {
        resolveAdmin(userId);
        CategoryAttribute ca = categoryDao.findCategoryAttribute(categoryId, attributeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Category-attribute link not found"));
        categoryDao.deleteCategoryAttribute(ca);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void resolveAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.PRODUCT_CATALOG_ADMIN_REQUIRED, "Access denied: STORE_ADMIN role required");
        }
    }
}
