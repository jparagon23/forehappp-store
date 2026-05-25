package com.forehapp.store.promotionModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.promotionModule.application.dto.CouponValidationResponse;
import com.forehapp.store.promotionModule.application.dto.RedeemCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.ValidateCouponRequestDto;
import com.forehapp.store.promotionModule.domain.model.Coupon;
import com.forehapp.store.promotionModule.domain.model.CouponRedemption;
import com.forehapp.store.promotionModule.domain.model.DiscountType;
import com.forehapp.store.promotionModule.domain.model.PromotionStatus;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionService;
import com.forehapp.store.promotionModule.domain.ports.out.ICouponDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class PromotionServiceImpl implements IPromotionService {

    private final ICouponDao couponDao;
    private final IStoreProfileDao storeProfileDao;

    public PromotionServiceImpl(ICouponDao couponDao, IStoreProfileDao storeProfileDao) {
        this.couponDao = couponDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(Long userId, ValidateCouponRequestDto dto) {
        StoreProfile profile = resolveProfile(userId);
        Coupon coupon = couponDao.findByCode(dto.code().toUpperCase())
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOT_FOUND, "Coupon not found"));

        String error = checkCouponRules(coupon, dto.storeId(), dto.orderAmount(), profile.getId());
        if (error != null) {
            throw new BadRequestException(ErrorCode.COUPON_INVALID, error);
        }

        BigDecimal discountAmount = calculateDiscount(coupon, dto.orderAmount());
        BigDecimal finalAmount = dto.orderAmount().subtract(discountAmount).max(BigDecimal.ZERO);

        return new CouponValidationResponse(
                true,
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                discountAmount,
                finalAmount,
                "Coupon applied successfully"
        );
    }

    @Override
    @Transactional
    public CouponValidationResponse redeemCoupon(Long userId, RedeemCouponRequestDto dto) {
        StoreProfile profile = resolveProfile(userId);
        Coupon coupon = couponDao.findByCode(dto.code().toUpperCase())
                .orElseThrow(() -> new NotFoundException(ErrorCode.COUPON_NOT_FOUND, "Coupon not found"));

        if (coupon.getStatus() == PromotionStatus.ACTIVE
                && coupon.getValidUntil() != null
                && LocalDate.now().isAfter(coupon.getValidUntil())) {
            coupon.setStatus(PromotionStatus.EXPIRED);
            couponDao.save(coupon);
        }

        String error = checkCouponRules(coupon, dto.storeId(), dto.orderAmount(), profile.getId());
        if (error != null) {
            throw new BadRequestException(ErrorCode.COUPON_INVALID, error);
        }

        BigDecimal discountAmount = calculateDiscount(coupon, dto.orderAmount());
        BigDecimal finalAmount = dto.orderAmount().subtract(discountAmount).max(BigDecimal.ZERO);

        CouponRedemption redemption = new CouponRedemption();
        redemption.setCoupon(coupon);
        redemption.setProfile(profile);
        redemption.setOrderId(dto.orderId());
        redemption.setDiscountApplied(discountAmount);
        couponDao.saveRedemption(redemption);

        coupon.setUsesCount(coupon.getUsesCount() + 1);
        couponDao.save(coupon);

        return new CouponValidationResponse(
                true,
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                discountAmount,
                finalAmount,
                "Coupon redeemed successfully"
        );
    }

    private String checkCouponRules(Coupon coupon, Long storeId, BigDecimal orderAmount, Long profileId) {
        if (!coupon.getStore().getId().equals(storeId)) {
            return "Coupon is not valid for this store";
        }
        if (coupon.getStatus() != PromotionStatus.ACTIVE) {
            return "Coupon is not active";
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(coupon.getValidFrom())) {
            return "Coupon is not yet valid";
        }
        if (coupon.getValidUntil() != null && today.isAfter(coupon.getValidUntil())) {
            return "Coupon has expired";
        }
        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            return "Order amount does not meet the minimum required: " + coupon.getMinOrderAmount();
        }
        if (coupon.getMaxUses() != null && coupon.getUsesCount() >= coupon.getMaxUses()) {
            return "Coupon has reached its usage limit";
        }
        long userUses = couponDao.countRedemptionsByCouponIdAndProfileId(coupon.getId(), profileId);
        if (userUses >= coupon.getMaxUsesPerUser()) {
            return "You have already used this coupon";
        }
        return null;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return orderAmount
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return coupon.getDiscountValue().min(orderAmount);
    }

    private StoreProfile resolveProfile(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
    }
}
