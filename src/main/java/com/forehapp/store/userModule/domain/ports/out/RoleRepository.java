package com.forehapp.store.userModule.domain.ports.out;

import com.forehapp.store.userModule.domain.model.Role;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findById(Long id);
}
