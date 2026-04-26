package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final IUserJpaRepository jpaRepository;

    public UserRepositoryAdapter(IUserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }
}
