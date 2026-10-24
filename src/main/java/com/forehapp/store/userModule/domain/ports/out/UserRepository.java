package com.forehapp.store.userModule.domain.ports.out;

import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.model.UserRegistrationSummary;
import com.forehapp.store.userModule.domain.model.UserSearchResult;
import com.forehapp.store.userModule.domain.model.RegistrationTrendPoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailWithActiveStoreProfile(String email);
    Optional<User> findById(Long id);
    User save(User user);
    long countAll();
    long countNewSince(LocalDateTime from);
    long countWithPhone();
    long countActive();
    List<UserRegistrationSummary> findRecentRegistrations(int limit);
    List<RegistrationTrendPoint> findRegistrationTrend(LocalDateTime from);

    /** Users whose name, lastname, email or store phone contain every term (case-insensitive). */
    List<UserSearchResult> search(List<String> terms, int limit);
}
