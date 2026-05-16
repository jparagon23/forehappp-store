package com.forehapp.store.wishlistModule.application.usecases;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.wishlistModule.application.dto.AddToWishlistDto;
import com.forehapp.store.wishlistModule.application.dto.WishlistItemResponse;
import com.forehapp.store.wishlistModule.application.dto.WishlistResponse;
import com.forehapp.store.wishlistModule.domain.model.Wishlist;
import com.forehapp.store.wishlistModule.domain.model.WishlistItem;
import com.forehapp.store.wishlistModule.domain.ports.in.IWishlistService;
import com.forehapp.store.wishlistModule.domain.ports.out.IWishlistDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WishlistServiceImpl implements IWishlistService {

    private static final int MAX_WISHLIST_ITEMS = 500;

    private final IWishlistDao wishlistDao;
    private final IProductDao productDao;
    private final IStoreProfileDao storeProfileDao;

    public WishlistServiceImpl(IWishlistDao wishlistDao,
                               IProductDao productDao,
                               IStoreProfileDao storeProfileDao) {
        this.wishlistDao = wishlistDao;
        this.productDao = productDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(Long userId) {
        StoreProfile owner = resolveProfile(userId);
        return wishlistDao.findByOwnerId(owner.getId())
                .map(this::toResponse)
                .orElse(emptyResponse());
    }

    @Override
    @Transactional
    public WishlistResponse addItem(Long userId, AddToWishlistDto dto) {
        StoreProfile owner = resolveProfile(userId);
        Product product = productDao.findById(dto.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Wishlist wishlist = wishlistDao.findByOwnerId(owner.getId()).orElseGet(() -> {
            Wishlist w = new Wishlist();
            w.setOwner(owner);
            return w;
        });

        boolean alreadyAdded = wishlist.getItems().stream()
                .anyMatch(i -> i.getProduct().getId().equals(product.getId()));
        if (alreadyAdded) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already in wishlist");
        }

        // BUG-E: cap wishlist size to avoid unbounded growth
        if (wishlist.getItems().size() >= MAX_WISHLIST_ITEMS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La wishlist no puede superar los " + MAX_WISHLIST_ITEMS + " productos");
        }

        WishlistItem item = new WishlistItem();
        item.setWishlist(wishlist);
        item.setProduct(product);
        wishlist.getItems().add(item);

        return toResponse(wishlistDao.save(wishlist));
    }

    @Override
    @Transactional
    public WishlistResponse removeItem(Long userId, Long itemId) {
        StoreProfile owner = resolveProfile(userId);
        Wishlist wishlist = requireWishlist(owner.getId());
        WishlistItem item = findItem(wishlist, itemId);
        wishlist.getItems().remove(item);
        return toResponse(wishlistDao.save(wishlist));
    }

    @Override
    @Transactional
    public void clearWishlist(Long userId) {
        StoreProfile owner = resolveProfile(userId);
        // BUG-D: only save when there's something to clear
        wishlistDao.findByOwnerId(owner.getId()).ifPresent(wishlist -> {
            if (!wishlist.getItems().isEmpty()) {
                wishlist.getItems().clear();
                wishlistDao.save(wishlist);
            }
        });
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private StoreProfile resolveProfile(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
    }

    private Wishlist requireWishlist(Long ownerId) {
        return wishlistDao.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found"));
    }

    private WishlistItem findItem(Wishlist wishlist, Long itemId) {
        return wishlist.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in wishlist"));
    }

    private WishlistResponse toResponse(Wishlist wishlist) {
        // BUG-B: exclude inactive/draft/out_of_stock products silently
        List<WishlistItemResponse> items = wishlist.getItems().stream()
                .filter(i -> i.getProduct().getStatus() == ProductStatus.ACTIVE)
                .map(this::toItemResponse)
                .toList();
        return new WishlistResponse(wishlist.getId(), items.size(), items);
    }

    private WishlistItemResponse toItemResponse(WishlistItem item) {
        Product product = item.getProduct();
        BigDecimal minPrice = product.getVariants().stream()
                .map(v -> v.getPrice())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return new WishlistItemResponse(
                item.getId(),
                product.getId(),
                product.getTitle(),
                minPrice,
                product.getVariants().size(),
                item.getAddedAt()
        );
    }

    private WishlistResponse emptyResponse() {
        return new WishlistResponse(null, 0, List.of());
    }
}
