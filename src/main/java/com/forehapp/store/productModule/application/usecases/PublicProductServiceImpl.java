package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.general.storage.StorageService;
import com.forehapp.store.productModule.application.dto.ProductImageResponse;
import com.forehapp.store.productModule.application.dto.PublicProductDetailResponse;
import com.forehapp.store.productModule.application.dto.PublicProductSummaryResponse;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductImage;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.ports.in.IProductImageService;
import com.forehapp.store.productModule.domain.ports.in.IPublicProductService;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
public class PublicProductServiceImpl implements IPublicProductService {

    private final IProductDao productDao;
    private final IProductImageService imageService;
    private final StorageService storageService;

    public PublicProductServiceImpl(IProductDao productDao, IProductImageService imageService, StorageService storageService) {
        this.productDao = productDao;
        this.imageService = imageService;
        this.storageService = storageService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicProductSummaryResponse> findActiveProducts(String search, Long categoryId, Long brandId, Pageable pageable) {
        return productDao.findActiveProducts(search, categoryId, brandId, pageable)
                .map(p -> {
                    String thumbnail = p.getImages().stream()
                            .findFirst()
                            .map(img -> storageService.presign(img.getS3Key(), Duration.ofDays(7)))
                            .orElse(null);
                    return new PublicProductSummaryResponse(p, thumbnail);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProductDetailResponse findActiveProductById(Long productId) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new NotFoundException("Product not found");
        }

        List<ProductImageResponse> images = imageService.getByProduct(productId);

        return new PublicProductDetailResponse(product, images);
    }
}
