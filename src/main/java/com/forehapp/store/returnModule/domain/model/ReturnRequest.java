package com.forehapp.store.returnModule.domain.model;

import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_return_requests")
@Getter @Setter
@NoArgsConstructor
public class ReturnRequest {

    @Id
    @Column(name = "return_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, unique = true)
    private OrderSellerGroup orderGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private StoreProfile buyer;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", nullable = false, length = 30)
    private ReturnType returnType;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "refund_amount", precision = 14, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status = ReturnStatus.PENDIENTE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
