package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUserAddressJpaRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByStoreProfileId(Long storeProfileId);

    Optional<UserAddress> findByStoreProfileIdAndIsDefaultTrue(Long storeProfileId);

    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.storeProfile.id = :storeProfileId")
    void clearDefaultForProfile(@Param("storeProfileId") Long storeProfileId);
}
