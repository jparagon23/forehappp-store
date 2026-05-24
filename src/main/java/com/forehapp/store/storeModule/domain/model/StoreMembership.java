package com.forehapp.store.storeModule.domain.model;

import com.forehapp.store.userModule.domain.model.StoreProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "store_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "store_profile_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class StoreMembership {

    @Id
    @Column(name = "membership_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_profile_id", nullable = false)
    private StoreProfile storeProfile;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StoreMemberRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    public void prePersist() {
        joinedAt = LocalDateTime.now();
    }
}
