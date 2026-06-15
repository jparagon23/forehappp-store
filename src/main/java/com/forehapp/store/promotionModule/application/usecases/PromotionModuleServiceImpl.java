package com.forehapp.store.promotionModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.promotionModule.application.dto.CouponResponse;
import com.forehapp.store.promotionModule.application.dto.CreateCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.UpdateCouponRequestDto;
import com.forehapp.store.promotionModule.domain.model.Coupon;
import com.forehapp.store.promotionModule.domain.model.DiscountType;
import com.forehapp.store.promotionModule.domain.model.PromotionStatus;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionModuleService;
import com.forehapp.store.promotionModule.domain.ports.out.ICouponDao;
import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.model.StoreMembership;
import com.forehapp.store.storeModule.domain.ports.out.IStoreDao;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionModuleServiceImpl implements IPromotionModuleService {

    private final ICouponDao couponDao;
    private final IStoreMembershipDao membershipDao;
    private final IStoreProfileDao storeProfileDao;
    private final IStoreDao storeDao;

    public PromotionModuleServiceImpl(ICouponDao couponDao,
                                      IStoreMembershipDao membershipDao,
                                      IStoreProfileDao storeProfileDao,
                                      IStoreDao storeDao) {
        this.couponDao = couponDao;
        this.membershipDao = membershipDao;
        this.storeProfileDao = storeProfileDao;
        this.storeDao = storeDao;
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(Long storeId, Long userId, CreateCouponRequestDto dto) {
        Store store = resolveStoreAccess(storeId, userId).getStore();

        if (couponDao.findByCode(dto.code().toUpperCase()).isPresent()) {
            throw new ConflictException(ErrorCode.COUPON_CODE_DUPLICATE, "Coupon code already exists");
        }
        validateCreateRequest(dto);

        Coupon coupon = new Coupon();
        coupon.setStore(store);
        populateCouponFields(coupon, dto);

        return toResponse(couponDao.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long storeId, Long userId, Long couponId, UpdateCouponRequestDto dto) {
        resolveStoreAccess(storeId, userId);
        Coupon coupon = findStoreCoupon(couponId, storeId);

        if (dto.description() != null) coupon.setDescription(dto.description());
        if (dto.minOrderAmount() != null) coupon.setMinOrderAmount(dto.minOrderAmount());
        if (dto.maxUses() != null) coupon.setMaxUses(dto.maxUses());
        if (dto.validUntil() != null) coupon.setValidUntil(dto.validUntil());
        if (dto.status() != null) coupon.setStatus(dto.status());

        return toResponse(couponDao.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse deactivateCoupon(Long storeId, Long userId, Long couponId) {
        resolveStoreAccess(storeId, userId);
        Coupon coupon = findStoreCoupon(couponId, storeId);
        coupon.setStatus(PromotionStatus.INACTIVE);
        return toResponse(couponDao.save(coupon));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> listMyCoupons(Long storeId, Long userId, int page, int size) {
        resolveStoreAccess(storeId, userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return couponDao.findByStoreId(storeId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long storeId, Long userId, Long couponId) {
        resolveStoreAccess(storeId, userId);
        return toResponse(findStoreCoupon(couponId, storeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> listAllCoupons(Long userId, int page, int size) {
        requireAdmin(userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return couponDao.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public CouponResponse adminCreateCoupon(Long adminUserId, Long storeId, CreateCouponRequestDto dto) {
        requireAdmin(adminUserId);
        Store store = storeDao.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND, "Store not found"));

        if (couponDao.findByCode(dto.code().toUpperCase()).isPresent()) {
            throw new ConflictException(ErrorCode.COUPON_CODE_DUPLICATE, "Coupon code already exists");
        }
        validateCreateRequest(dto);

        Coupon coupon = new Coupon();
        coupon.setStore(store);
        populateCouponFields(coupon, dto);

        return toResponse(couponDao.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse adminUpdateCoupon(Long adminUserId, Long couponId, UpdateCouponRequestDto dto) {
        requireAdmin(adminUserId);
        Coupon coupon = couponDao.findById(couponId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOT_FOUND, "Coupon not found"));

        if (dto.description() != null) coupon.setDescription(dto.description());
        if (dto.minOrderAmount() != null) coupon.setMinOrderAmount(dto.minOrderAmount());
        if (dto.maxUses() != null) coupon.setMaxUses(dto.maxUses());
        if (dto.validUntil() != null) coupon.setValidUntil(dto.validUntil());
        if (dto.status() != null) coupon.setStatus(dto.status());

        return toResponse(couponDao.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse adminDeactivateCoupon(Long adminUserId, Long couponId) {
        requireAdmin(adminUserId);
        Coupon coupon = couponDao.findById(couponId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOT_FOUND, "Coupon not found"));
        coupon.setStatus(PromotionStatus.INACTIVE);
        return toResponse(couponDao.save(coupon));
    }

    private StoreMembership resolveStoreAccess(Long storeId, Long userId) {
        return membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED,
                        "You are not an active member of this store"));
    }

    private Coupon findStoreCoupon(Long couponId, Long storeId) {
        Coupon coupon = couponDao.findById(couponId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOT_FOUND, "Coupon not found"));
        if (!coupon.getStore().getId().equals(storeId)) {
            throw new ForbiddenException(ErrorCode.COUPON_ACCESS_DENIED, "This coupon does not belong to this store");
        }
        return coupon;
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
    }

    private void validateCreateRequest(CreateCouponRequestDto dto) {
        if (dto.discountType() != DiscountType.FREE_SHIPPING && dto.discountValue() == null) {
            throw new BadRequestException(ErrorCode.COUPON_INVALID,
                    "discountValue is required for PERCENTAGE and FIXED_AMOUNT coupons");
        }
        if (dto.validUntil() != null && dto.validUntil().isBefore(dto.validFrom())) {
            throw new BadRequestException(ErrorCode.COUPON_DATE_INVALID, "validUntil must be after validFrom");
        }
    }

    private void populateCouponFields(Coupon coupon, CreateCouponRequestDto dto) {
        coupon.setCode(dto.code().toUpperCase());
        coupon.setDescription(dto.description());
        coupon.setDiscountType(dto.discountType());
        coupon.setDiscountValue(dto.discountType() == DiscountType.FREE_SHIPPING
                ? java.math.BigDecimal.ZERO
                : dto.discountValue());
        coupon.setMinOrderAmount(dto.minOrderAmount());
        coupon.setMaxUses(dto.maxUses());
        coupon.setMaxUsesPerUser(dto.maxUsesPerUser());
        coupon.setValidFrom(dto.validFrom());
        coupon.setValidUntil(dto.validUntil());
        if (dto.assignedToProfileId() != null) {
            coupon.setAssignedToProfile(
                    storeProfileDao.findById(dto.assignedToProfileId())
                            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND,
                                    "Assigned profile not found"))
            );
        }
    }

    private CouponResponse toResponse(Coupon c) {
        return new CouponResponse(
                c.getId(),
                c.getStore().getId(),
                c.getStore().getName(),
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
                c.getCreatedAt(),
                c.getAssignedToProfile() != null ? c.getAssignedToProfile().getId() : null
        );
    }
}
