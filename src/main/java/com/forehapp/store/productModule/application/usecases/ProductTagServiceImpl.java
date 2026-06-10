package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductTag;
import com.forehapp.store.productModule.domain.ports.in.IProductTagService;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductTagServiceImpl implements IProductTagService {

    private final IProductDao productDao;
    private final IStoreMembershipDao membershipDao;

    public ProductTagServiceImpl(IProductDao productDao, IStoreMembershipDao membershipDao) {
        this.productDao = productDao;
        this.membershipDao = membershipDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTags(Long productId, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);
        return product.getTags().stream().map(ProductTag::getTag).toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"public-products", "public-product-brand-facets", "seller-products", "seller-product-detail"}, allEntries = true)
    public List<String> setTags(Long productId, List<String> rawTags, Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        Product product = resolveStoreProduct(productId, storeId);

        List<String> normalized = rawTags.stream()
                .map(t -> t.trim().toLowerCase())
                .filter(t -> !t.isBlank())
                .distinct()
                .toList();

        product.getTags().clear();
        normalized.forEach(tagValue -> {
            ProductTag pt = new ProductTag();
            pt.setProduct(product);
            pt.setTag(tagValue);
            product.getTags().add(pt);
        });

        productDao.save(product);
        return normalized;
    }

    private void resolveStoreAccess(Long storeId, Long userId) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .filter(m -> m.getRole() != StoreMemberRole.STAFF)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED,
                        "You do not have permission to manage this store's products"));
    }

    private Product resolveStoreProduct(Long productId, Long storeId) {
        return productDao.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found"));
    }
}
