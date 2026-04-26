package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
