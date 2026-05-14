package com.forehapp.store.promotionModule.infrastructure.persistence;

import com.forehapp.store.promotionModule.domain.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    Page<Coupon> findBySellerId(Long sellerId, Pageable pageable);
}
