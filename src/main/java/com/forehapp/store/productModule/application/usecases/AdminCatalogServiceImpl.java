package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.application.dto.*;
import com.forehapp.store.productModule.domain.model.*;
import com.forehapp.store.productModule.domain.ports.in.IAdminCatalogService;
import com.forehapp.store.productModule.domain.ports.out.*;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
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
    public LineResponse createLine(Long brandId, CreateLineRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Brand brand = brandDao.findById(brandId)
                .orElseThrow(() -> new NotFoundException("Brand not found"));
        Category category = categoryDao.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        Line line = new Line();
        line.setBrand(brand);
        line.setCategory(category);
        line.setDescription(dto.getName().trim());
        return new LineResponse(lineDao.save(line));
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
    public AttributeResponse createAttribute(CreateAttributeRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Attribute attribute = new Attribute();
        attribute.setDescription(dto.getName().trim());
        return new AttributeResponse(attributeDao.save(attribute));
    }

    @Override
    @Transactional
    public CategoryAttributeResponse.AttributeValueDto createAttributeValue(
            Long attributeId, CreateAttributeValueRequestDto dto, Long userId) {
        resolveAdmin(userId);
        Attribute attribute = attributeDao.findById(attributeId)
                .orElseThrow(() -> new NotFoundException("Attribute not found"));
        AttributeValue value = new AttributeValue();
        value.setAttribute(attribute);
        value.setDescription(dto.getDescription().trim());
        AttributeValue saved = attributeValueDao.save(value);
        return new CategoryAttributeResponse.AttributeValueDto(saved.getId(), saved.getDescription());
    }

    @Override
    @Transactional
    public CategoryAttributeResponse linkAttributeToCategory(
            Long categoryId, LinkCategoryAttributeRequestDto dto, Long userId) {
        resolveAdmin(userId);

        Category category = categoryDao.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        Attribute attribute = attributeDao.findById(dto.getAttributeId())
                .orElseThrow(() -> new NotFoundException("Attribute not found"));

        if (categoryDao.existsCategoryAttribute(categoryId, dto.getAttributeId())) {
            throw new BadRequestException("Attribute is already linked to this category");
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

    private void resolveAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new BadRequestException("Access denied: STORE_ADMIN role required");
        }
    }
}
