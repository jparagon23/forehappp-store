package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.Role;
import com.forehapp.store.userModule.domain.ports.out.RoleRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final IRoleJpaRepository jpaRepository;

    public RoleRepositoryAdapter(IRoleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Role> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
