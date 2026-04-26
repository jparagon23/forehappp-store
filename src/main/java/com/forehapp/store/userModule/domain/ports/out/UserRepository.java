package com.forehapp.store.userModule.domain.ports.out;

import com.forehapp.store.userModule.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
}
