package com.forehapp.store.inventoryModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.inventoryModule.application.dto.AdjustInventoryRequestDto;
import com.forehapp.store.inventoryModule.application.dto.InventoryMovementResponse;
import com.forehapp.store.inventoryModule.domain.ports.in.IInventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.forehapp.store.productModule.domain.model.*;
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
    public void adjustInventory(Long productId, Long variantId, AdjustInventoryRequestDto dto, Long userId) {
        if (dto.getQuantity() == 0) {
            throw new BadRequestException("Quantity must not be zero");
        }
        if (dto.getReason() == MovementReason.SALE) {
            throw new BadRequestException("SALE movements are handled internally");
        }
        if (dto.getReason() != MovementReason.ADJUSTMENT && dto.getQuantity() < 0) {
            throw new BadRequestException("Only ADJUSTMENT movements can have a negative quantity");
        }

        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Store profile not found"));

        boolean isAdmin = profile.getRoles().contains(StoreRole.STORE_ADMIN);
        boolean isSeller = profile.getRoles().contains(StoreRole.SELLER);

        if (!isAdmin && !isSeller) {
            throw new BadRequestException("User does not have permission to adjust inventory");
        }

        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (isSeller && !isAdmin && !product.getSeller().getId().equals(profile.getId())) {
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

    @Override
    public Page<InventoryMovementResponse> getMovements(Long productId, Long variantId,
                                                         MovementReason reason, Pageable pageable,
                                                         Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Store profile not found"));

        boolean isAdmin = profile.getRoles().contains(StoreRole.STORE_ADMIN);
        boolean isSeller = profile.getRoles().contains(StoreRole.SELLER);

        if (!isAdmin && !isSeller) {
            throw new BadRequestException("User does not have permission to view inventory movements");
        }

        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (isSeller && !isAdmin && !product.getSeller().getId().equals(profile.getId())) {
            throw new BadRequestException("Product does not belong to this seller");
        }

        variantDao.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new NotFoundException("Variant not found for this product"));

        return movementDao.findByVariant(variantId, reason, pageable)
                .map(this::toResponse);
    }

    private InventoryMovementResponse toResponse(InventoryMovement movement) {
        InventoryMovementResponse response = new InventoryMovementResponse();
        response.setId(movement.getId());
        response.setQuantity(movement.getQuantity());
        response.setReason(movement.getReason());
        response.setCreatedAt(movement.getCreatedAt());
        return response;
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
