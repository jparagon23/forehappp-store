package com.forehapp.store.donationModule.domain.model;

import com.forehapp.store.userModule.domain.model.StoreProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_donation_records")
@Getter @Setter
@NoArgsConstructor
public class DonationRecord {

    @Id
    @Column(name = "donation_record_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foundation_id", nullable = false)
    private DonationFoundation foundation;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "coupon_code", nullable = false, length = 50)
    private String couponCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_profile_id")
    private StoreProfile donorProfile;

    @Column(name = "donor_email", length = 255)
    private String donorEmail;

    @Column(name = "donation_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal donationAmount;

    @Column(name = "donation_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal donationPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DonationRecordStatus status = DonationRecordStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
