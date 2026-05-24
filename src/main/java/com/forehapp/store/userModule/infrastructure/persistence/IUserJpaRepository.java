package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IUserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND EXISTS (SELECT sp FROM StoreProfile sp WHERE sp.user = u AND sp.active = true)")
    Optional<User> findByEmailWithActiveStoreProfile(@Param("email") String email);
}
