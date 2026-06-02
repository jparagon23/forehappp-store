package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.RegistrationTrendPoint;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.model.UserRegistrationSummary;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
    public Optional<User> findByEmailWithActiveStoreProfile(String email) {
        return jpaRepository.findByEmailWithActiveStoreProfile(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public long countNewSince(LocalDateTime from) {
        return jpaRepository.countByCreationDateAfter(from);
    }

    @Override
    public long countWithPhone() {
        return jpaRepository.countUsersWithPhone();
    }

    @Override
    public long countActive() {
        return jpaRepository.countActiveUsers();
    }

    @Override
    public List<UserRegistrationSummary> findRecentRegistrations(int limit) {
        return jpaRepository.findRecentRegistrations(PageRequest.of(0, limit)).stream()
                .map(v -> new UserRegistrationSummary(v.getId(), v.getName(), v.getLastname(),
                        v.getEmail(), v.getPhone(), v.getCreationDate()))
                .toList();
    }

    @Override
    public List<RegistrationTrendPoint> findRegistrationTrend(LocalDateTime from) {
        return jpaRepository.findRegistrationTrend(from).stream()
                .map(v -> new RegistrationTrendPoint(v.getDate(), v.getCount()))
                .toList();
    }
}
