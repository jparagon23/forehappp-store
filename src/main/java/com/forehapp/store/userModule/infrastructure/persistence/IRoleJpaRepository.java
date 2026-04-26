package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleJpaRepository extends JpaRepository<Role, Long> {
}
