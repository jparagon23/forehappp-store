package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.application.dto.ProductImageResponse;
import com.forehapp.store.productModule.application.dto.PublicProductDetailResponse;
import com.forehapp.store.productModule.application.dto.PublicProductSummaryResponse;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.ports.in.IPublicProductService;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.productModule.domain.ports.out.IProductImageDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicProductServiceImpl implements IPublicProductService {

    private final IProductDao productDao;
    private final IProductImageDao productImageDao;

    public PublicProductServiceImpl(IProductDao productDao, IProductImageDao productImageDao) {
        this.productDao = productDao;
        this.productImageDao = productImageDao;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicProductSummaryResponse> findActiveProducts(String search, Long categoryId, Long brandId, Pageable pageable) {
        return productDao.findActiveProducts(search, categoryId, brandId, pageable)
                .map(PublicProductSummaryResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProductDetailResponse findActiveProductById(Long productId) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new NotFoundException("Product not found");
        }

        List<ProductImageResponse> images = productImageDao.findByProductId(productId).stream()
                .map(ProductImageResponse::new)
                .toList();

        return new PublicProductDetailResponse(product, images);
    }
}
