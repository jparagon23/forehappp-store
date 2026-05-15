package com.forehapp.store.cartModule.application.usecases;

import com.forehapp.store.cartModule.application.dto.AddItemRequestDto;
import com.forehapp.store.cartModule.application.dto.CartItemResponse;
import com.forehapp.store.cartModule.application.dto.CartResponse;
import com.forehapp.store.cartModule.application.dto.CartSellerGroupResponse;
import com.forehapp.store.cartModule.application.dto.UpdateCartItemDto;
import com.forehapp.store.cartModule.domain.model.Cart;
import com.forehapp.store.cartModule.domain.model.CartItem;
import com.forehapp.store.cartModule.domain.model.CartStatus;
import com.forehapp.store.cartModule.domain.ports.in.ICartService;
import com.forehapp.store.cartModule.domain.ports.out.ICartDao;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements ICartService {

    private static final int MAX_ITEM_QUANTITY = 9999;

    private final ICartDao cartDao;
    private final IProductVariantDao productVariantDao;
    private final IStoreProfileDao storeProfileDao;

    public CartServiceImpl(ICartDao cartDao,
                           IProductVariantDao productVariantDao,
                           IStoreProfileDao storeProfileDao) {
        this.cartDao = cartDao;
        this.productVariantDao = productVariantDao;
        this.storeProfileDao = storeProfileDao;
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
        ProductVariant variant = productVariantDao.findById(dto.variantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variant not found"));

        Cart cart = findValidCart(buyer.getId()).orElseGet(() -> {
            Cart c = new Cart();
            c.setBuyer(buyer);
            return c;
        });

        cart.getItems().stream()
                .filter(i -> i.getVariant().getId().equals(variant.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            // BUG-01: guard against Integer overflow on quantity accumulation
                            int newQty = existing.getQuantity() + dto.quantity();
                            if (newQty > MAX_ITEM_QUANTITY) {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
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

        return toResponse(saveCart(cart));
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
        findValidCart(buyer.getId()).ifPresent(cart -> {
            cart.getItems().clear();
            saveCart(cart);
        });
    }

    // ── helpers ────────────────────────────────────────────────────────────

    // BUG-05: restrict cart usage to profiles with CUSTOMER role
    private StoreProfile requireBuyer(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.CUSTOMER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo compradores pueden usar el carrito");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }

    private Cart saveCart(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        return cartDao.save(cart);
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart"));
    }

    private CartResponse toResponse(Cart cart) {
        // BUG-06: exclude items whose product is no longer ACTIVE
        Map<Long, List<CartItem>> bySeller = cart.getItems().stream()
                .filter(i -> i.getVariant().getProduct().getStatus() == ProductStatus.ACTIVE)
                .collect(Collectors.groupingBy(i -> i.getVariant().getProduct().getSeller().getId()));

        List<CartSellerGroupResponse> groups = bySeller.entrySet().stream()
                .map(entry -> {
                    StoreProfile seller = entry.getValue().get(0).getVariant().getProduct().getSeller();
                    // BUG-04: build sellerName safely — name or lastname may be null
                    String sellerName = Stream.of(seller.getUser().getName(), seller.getUser().getLastname())
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" "));
                    List<CartItemResponse> itemResponses = entry.getValue().stream()
                            .map(this::toItemResponse)
                            .toList();
                    BigDecimal subtotal = itemResponses.stream()
                            .map(CartItemResponse::subtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CartSellerGroupResponse(entry.getKey(), sellerName, itemResponses, subtotal);
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

        return new CartItemResponse(
                item.getId(),
                variant.getId(),
                variant.getSku(),
                variant.getProduct().getTitle(),
                item.getQuantity(),
                currentPrice,
                subtotal,
                priceChanged,
                priceChanged ? item.getPriceAtAdd() : null
        );
    }

    private CartResponse emptyCartResponse() {
        return new CartResponse(null, CartStatus.ACTIVE.name(), null, BigDecimal.ZERO, List.of());
    }
}
