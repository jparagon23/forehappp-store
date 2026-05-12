package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.application.dto.AddInventoryRequestDto;
import com.forehapp.store.productModule.domain.model.*;
import com.forehapp.store.productModule.domain.ports.in.IInventoryService;
import com.forehapp.store.productModule.domain.ports.out.IInventoryMovementDao;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements IInventoryService {

    private final IProductDao productDao;
    private final IProductVariantDao variantDao;
    private final IInventoryMovementDao movementDao;
    private final IStoreProfileDao storeProfileDao;

    public InventoryServiceImpl(IProductDao productDao,
                                IProductVariantDao variantDao,
                                IInventoryMovementDao movementDao,
                                IStoreProfileDao storeProfileDao) {
        this.productDao = productDao;
        this.variantDao = variantDao;
        this.movementDao = movementDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public void addInventory(Long productId, Long variantId, AddInventoryRequestDto dto, Long userId) {
        if (dto.getQuantity() == 0) {
            throw new BadRequestException("Quantity must not be zero");
        }
        if (dto.getReason() == MovementReason.SALE) {
            throw new BadRequestException("SALE movements are handled internally");
        }
        if (dto.getReason() != MovementReason.ADJUSTMENT && dto.getQuantity() < 0) {
            throw new BadRequestException("Only ADJUSTMENT movements can have a negative quantity");
        }

        StoreProfile seller = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Store profile not found"));

        if (!seller.getRoles().contains(StoreRole.SELLER)) {
            throw new BadRequestException("User does not have SELLER role");
        }

        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new BadRequestException("Product does not belong to this seller");
        }

        ProductVariant variant = variantDao.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new NotFoundException("Variant not found for this product"));

        int resultingStock = variant.getStock() + dto.getQuantity();
        if (resultingStock < 0) {
            throw new BadRequestException("Insufficient stock. Current: " + variant.getStock()
                    + ", adjustment: " + dto.getQuantity());
        }

        InventoryMovement movement = new InventoryMovement();
        movement.setVariant(variant);
        movement.setQuantity(dto.getQuantity());
        movement.setReason(dto.getReason());
        movementDao.save(movement);
        movementDao.incrementStock(variantId, dto.getQuantity());

        syncProductStockStatus(product);
    }

    private void syncProductStockStatus(Product product) {
        boolean allEmpty = product.getVariants().stream()
                .allMatch(v -> v.getStock() == 0);

        if (allEmpty && product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
            productDao.save(product);
        } else if (!allEmpty && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
            productDao.save(product);
        }
    }
}
