package com.forehapp.store.promotionModule.application.usecases;

import com.forehapp.store.promotionModule.application.dto.CouponResponse;
import com.forehapp.store.promotionModule.application.dto.CreateCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.UpdateCouponRequestDto;
import com.forehapp.store.promotionModule.domain.model.Coupon;
import com.forehapp.store.promotionModule.domain.model.PromotionStatus;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionModuleService;
import com.forehapp.store.promotionModule.domain.ports.out.ICouponDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PromotionModuleServiceImpl implements IPromotionModuleService {

    private final ICouponDao couponDao;
    private final IStoreProfileDao storeProfileDao;

    public PromotionModuleServiceImpl(ICouponDao couponDao, IStoreProfileDao storeProfileDao) {
        this.couponDao = couponDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(Long userId, CreateCouponRequestDto dto) {
        StoreProfile seller = requireSeller(userId);

        if (couponDao.findByCode(dto.code().toUpperCase()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Coupon code already exists");
        }
        if (dto.validUntil() != null && dto.validUntil().isBefore(dto.validFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validUntil must be after validFrom");
        }

        Coupon coupon = new Coupon();
        coupon.setSeller(seller);
        coupon.setCode(dto.code().toUpperCase());
        coupon.setDescription(dto.description());
        coupon.setDiscountType(dto.discountType());
        coupon.setDiscountValue(dto.discountValue());
        coupon.setMinOrderAmount(dto.minOrderAmount());
        coupon.setMaxUses(dto.maxUses());
        coupon.setMaxUsesPerUser(dto.maxUsesPerUser());
        coupon.setValidFrom(dto.validFrom());
        coupon.setValidUntil(dto.validUntil());

        return toResponse(couponDao.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long userId, Long couponId, UpdateCouponRequestDto dto) {
        StoreProfile seller = requireSeller(userId);
        Coupon coupon = findOwnedCoupon(couponId, seller.getId());

        if (dto.description() != null) coupon.setDescription(dto.description());
        if (dto.minOrderAmount() != null) coupon.setMinOrderAmount(dto.minOrderAmount());
        if (dto.maxUses() != null) coupon.setMaxUses(dto.maxUses());
        if (dto.validUntil() != null) coupon.setValidUntil(dto.validUntil());
        if (dto.status() != null) coupon.setStatus(dto.status());

        return toResponse(couponDao.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse deactivateCoupon(Long userId, Long couponId) {
        StoreProfile seller = requireSeller(userId);
        Coupon coupon = findOwnedCoupon(couponId, seller.getId());
        coupon.setStatus(PromotionStatus.INACTIVA);
        return toResponse(couponDao.save(coupon));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> listMyCoupons(Long userId, int page, int size) {
        StoreProfile seller = requireSeller(userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return couponDao.findBySellerId(seller.getId(), pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long userId, Long couponId) {
        StoreProfile seller = requireSeller(userId);
        return toResponse(findOwnedCoupon(couponId, seller.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> listAllCoupons(Long userId, int page, int size) {
        requireAdmin(userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return couponDao.findAll(pageable).map(this::toResponse);
    }

    private Coupon findOwnedCoupon(Long couponId, Long sellerId) {
        Coupon coupon = couponDao.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        if (!coupon.getSeller().getId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This coupon does not belong to you");
        }
        return coupon;
    }

    private StoreProfile requireSeller(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.SELLER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller access required");
        }
        return profile;
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private CouponResponse toResponse(Coupon c) {
        String sellerName = c.getSeller().getUser().getName() + " " + c.getSeller().getUser().getLastname();
        return new CouponResponse(
                c.getId(),
                c.getSeller().getId(),
                sellerName.trim(),
                c.getCode(),
                c.getDescription(),
                c.getDiscountType().name(),
                c.getDiscountValue(),
                c.getMinOrderAmount(),
                c.getMaxUses(),
                c.getUsesCount(),
                c.getMaxUsesPerUser(),
                c.getValidFrom(),
                c.getValidUntil(),
                c.getStatus().name(),
                c.getCreatedAt()
        );
    }
}
