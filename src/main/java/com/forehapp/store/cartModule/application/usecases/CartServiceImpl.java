package com.forehapp.store.cartModule.application.usecases;

import com.forehapp.store.cartModule.application.dto.AddItemRequestDto;
import com.forehapp.store.cartModule.application.dto.CartItemResponse;
import com.forehapp.store.cartModule.application.dto.CartResponse;
import com.forehapp.store.cartModule.application.dto.CartSellerGroupResponse;
import com.forehapp.store.cartModule.application.dto.ShippingEstimateGroupResponse;
import com.forehapp.store.cartModule.application.dto.ShippingEstimateResponse;
import com.forehapp.store.cartModule.application.dto.UpdateCartItemDto;
import com.forehapp.store.cartModule.domain.model.Cart;
import com.forehapp.store.cartModule.domain.model.CartItem;
import com.forehapp.store.cartModule.domain.model.CartStatus;
import com.forehapp.store.cartModule.domain.ports.in.ICartService;
import com.forehapp.store.cartModule.domain.ports.out.ICartDao;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.general.storage.StorageService;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.shippingModule.domain.model.ShippingZone;
import com.forehapp.store.shippingModule.domain.ports.out.IShippingZoneDao;
import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.model.UserAddress;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.IUserAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class CartServiceImpl implements ICartService {

    private static final int MAX_ITEM_QUANTITY = 9999;

    private final ICartDao cartDao;
    private final IProductVariantDao productVariantDao;
    private final IStoreProfileDao storeProfileDao;
    private final StorageService storageService;
    private final IShippingZoneDao shippingZoneDao;
    private final IUserAddressRepository addressRepository;

    public CartServiceImpl(ICartDao cartDao,
                           IProductVariantDao productVariantDao,
                           IStoreProfileDao storeProfileDao,
                           StorageService storageService,
                           IShippingZoneDao shippingZoneDao,
                           IUserAddressRepository addressRepository) {
        this.cartDao = cartDao;
        this.productVariantDao = productVariantDao;
        this.storeProfileDao = storeProfileDao;
        this.storageService = storageService;
        this.shippingZoneDao = shippingZoneDao;
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional
    public CartResponse getCart(Long userId) {
        StoreProfile buyer = requireBuyer(userId);
        return findValidCart(buyer.getId())
                .map(this::toResponse)
                .orElse(emptyCartResponse());
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddItemRequestDto dto) {
        StoreProfile buyer = requireBuyer(userId);
        Cart cart = findValidCart(buyer.getId()).orElseGet(() -> {
            Cart c = new Cart();
            c.setBuyer(buyer);
            return c;
        });
        mergeItem(cart, dto);
        return toResponse(saveCart(cart));
    }

    @Override
    @Transactional
    public CartResponse addItems(Long userId, List<AddItemRequestDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException(ErrorCode.CART_ITEMS_EMPTY, "Items list must not be empty");
        }
        // BUG-BATCH-02: cap batch size to avoid N unbounded DB queries
        if (dtos.size() > 50) {
            throw new BadRequestException(ErrorCode.CART_ITEM_LIMIT_EXCEEDED,
                    "El batch no puede superar 50 ítems por solicitud");
        }
        StoreProfile buyer = requireBuyer(userId);
        Cart cart = findValidCart(buyer.getId()).orElseGet(() -> {
            Cart c = new Cart();
            c.setBuyer(buyer);
            return c;
        });
        dtos.forEach(dto -> mergeItem(cart, dto));
        return toResponse(saveCart(cart));
    }

    private void mergeItem(Cart cart, AddItemRequestDto dto) {
        // BUG-BATCH-01: @Valid does not cascade into List elements in Spring MVC —
        // validate manually so batch and single-add behave identically
        if (dto.variantId() == null) {
            throw new BadRequestException(ErrorCode.CART_VARIANT_REQUIRED, "variantId must not be null");
        }
        if (dto.quantity() == null || dto.quantity() < 1 || dto.quantity() > MAX_ITEM_QUANTITY) {
            throw new BadRequestException(ErrorCode.CART_ITEM_LIMIT_EXCEEDED,
                    "quantity debe estar entre 1 y " + MAX_ITEM_QUANTITY);
        }
        ProductVariant variant = productVariantDao.findById(dto.variantId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CART_VARIANT_NOT_FOUND,
                        "Variant not found: " + dto.variantId()));

        if (!Boolean.TRUE.equals(variant.getActive()) ||
                variant.getProduct().getStatus() != com.forehapp.store.productModule.domain.model.ProductStatus.ACTIVE) {
            throw new BadRequestException(ErrorCode.CART_INACTIVE_PRODUCT, "This item is no longer available");
        }

        cart.getItems().stream()
                .filter(i -> i.getVariant().getId().equals(variant.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            int newQty = existing.getQuantity() + dto.quantity();
                            if (newQty > MAX_ITEM_QUANTITY) {
                                throw new BadRequestException(ErrorCode.CART_ITEM_LIMIT_EXCEEDED,
                                        "La cantidad máxima por producto es " + MAX_ITEM_QUANTITY);
                            }
                            existing.setQuantity(newQty);
                        },
                        () -> {
                            CartItem item = new CartItem();
                            item.setCart(cart);
                            item.setVariant(variant);
                            item.setQuantity(dto.quantity());
                            item.setPriceAtAdd(variant.getPrice());
                            cart.getItems().add(item);
                        }
                );
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemDto dto) {
        StoreProfile buyer = requireBuyer(userId);
        Cart cart = requireValidCart(buyer.getId());
        findItem(cart, itemId).setQuantity(dto.quantity());
        return toResponse(saveCart(cart));
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long itemId) {
        StoreProfile buyer = requireBuyer(userId);
        Cart cart = requireValidCart(buyer.getId());
        cart.getItems().remove(findItem(cart, itemId));
        saveCart(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        StoreProfile buyer = requireBuyer(userId);
        // NUEVA-BUG-03: only persist if there were actually items to remove
        findValidCart(buyer.getId()).ifPresent(cart -> {
            if (!cart.getItems().isEmpty()) {
                cart.getItems().clear();
                saveCart(cart);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingEstimateResponse estimateShipping(Long userId, Long addressId) {
        StoreProfile buyer = requireBuyer(userId);

        Cart cart = findValidCart(buyer.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CART_NOT_FOUND, "Active cart not found"));

        UserAddress address = addressRepository.findById(addressId)
                .filter(a -> a.getStoreProfile().getId().equals(buyer.getId()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_ADDRESS_NOT_FOUND, "Address not found"));

        Long cityId = address.getCity().getId();

        Map<Long, List<CartItem>> byStore = cart.getItems().stream()
                .filter(i -> i.getVariant().getProduct().getStatus() == ProductStatus.ACTIVE
                        && Boolean.TRUE.equals(i.getVariant().getActive()))
                .collect(Collectors.groupingBy(i -> i.getVariant().getProduct().getStore().getId()));

        List<ShippingEstimateGroupResponse> groups = byStore.entrySet().stream()
                .map(entry -> {
                    Store store = entry.getValue().get(0).getVariant().getProduct().getStore();
                    List<CartItem> items = entry.getValue();
                    BigDecimal subtotal = items.stream()
                            .map(i -> i.getVariant().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal shippingCost = resolveShippingCost(cityId, store, subtotal, items);
                    return new ShippingEstimateGroupResponse(
                            store.getId(),
                            store.getName(),
                            subtotal,
                            shippingCost,
                            shippingCost.compareTo(BigDecimal.ZERO) == 0
                    );
                })
                .toList();

        BigDecimal itemsTotal = groups.stream()
                .map(ShippingEstimateGroupResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingTotal = groups.stream()
                .map(ShippingEstimateGroupResponse::shippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShippingEstimateResponse(groups, itemsTotal, shippingTotal, itemsTotal.add(shippingTotal));
    }

    private BigDecimal resolveShippingCost(Long cityId, Store store, BigDecimal subtotal, List<CartItem> items) {
        if (store.getFreeShippingMinAmount() != null
                && subtotal.compareTo(store.getFreeShippingMinAmount()) >= 0) {
            return BigDecimal.ZERO;
        }
        boolean allFree = items.stream()
                .allMatch(i -> Boolean.TRUE.equals(i.getVariant().getProduct().getFreeShipping()));
        if (allFree) {
            return BigDecimal.ZERO;
        }
        return shippingZoneDao.findActiveByCityId(cityId)
                .or(shippingZoneDao::findActiveDefault)
                .map(ShippingZone::getCost)
                .orElse(BigDecimal.ZERO);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    // BUG-05: restrict cart usage to profiles with CUSTOMER role
    private StoreProfile requireBuyer(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.CUSTOMER)) {
            throw new ForbiddenException(ErrorCode.CART_ACCESS_DENIED, "Solo compradores pueden usar el carrito");
        }
        return profile;
    }

    private Optional<Cart> findValidCart(Long buyerId) {
        return cartDao.findActiveByBuyerId(buyerId).flatMap(cart -> {
            if (cart.getUpdatedAt().isBefore(LocalDateTime.now().minusDays(180))) {
                cart.setStatus(CartStatus.EXPIRED);
                cartDao.save(cart);
                return Optional.empty();
            }
            return Optional.of(cart);
        });
    }

    private Cart requireValidCart(Long buyerId) {
        return findValidCart(buyerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CART_NOT_FOUND, "Cart not found"));
    }

    private Cart saveCart(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        return cartDao.save(cart);
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCode.CART_ITEM_NOT_FOUND, "Item not found in cart"));
    }

    private CartResponse toResponse(Cart cart) {
        // BUG-06: exclude items whose product is no longer ACTIVE
        Map<Long, List<CartItem>> byStore = cart.getItems().stream()
                .filter(i -> i.getVariant().getProduct().getStatus() == ProductStatus.ACTIVE
                        && Boolean.TRUE.equals(i.getVariant().getActive()))
                .collect(Collectors.groupingBy(i -> i.getVariant().getProduct().getStore().getId()));

        List<CartSellerGroupResponse> groups = byStore.entrySet().stream()
                .map(entry -> {
                    Store store = entry.getValue().get(0).getVariant().getProduct().getStore();
                    List<CartItemResponse> itemResponses = entry.getValue().stream()
                            .map(this::toItemResponse)
                            .toList();
                    BigDecimal subtotal = itemResponses.stream()
                            .map(CartItemResponse::subtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CartSellerGroupResponse(entry.getKey(), store.getName(), itemResponses, subtotal);
                })
                .toList();

        BigDecimal total = groups.stream()
                .map(CartSellerGroupResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getStatus().name(), cart.getUpdatedAt(), total, groups);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        ProductVariant variant = item.getVariant();
        // BUG-02: guard against null price (variant.price is nullable=false but defensive)
        BigDecimal currentPrice = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;
        boolean priceChanged = currentPrice.compareTo(item.getPriceAtAdd()) != 0;
        BigDecimal subtotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        List<com.forehapp.store.productModule.domain.model.ProductImage> images = variant.getProduct().getImages();
        String thumbnailUrl = null;
        if (!images.isEmpty()) {
            String signed = storageService.presign(images.get(0).getS3Key(), java.time.Duration.ofDays(7));
            thumbnailUrl = signed.isBlank() ? null : signed;
        }

        return new CartItemResponse(
                item.getId(),
                variant.getId(),
                variant.getSku(),
                variant.getProduct().getTitle(),
                item.getQuantity(),
                currentPrice,
                subtotal,
                priceChanged,
                priceChanged ? item.getPriceAtAdd() : null,
                thumbnailUrl
        );
    }

    private CartResponse emptyCartResponse() {
        return new CartResponse(null, CartStatus.ACTIVE.name(), null, BigDecimal.ZERO, List.of());
    }
}
