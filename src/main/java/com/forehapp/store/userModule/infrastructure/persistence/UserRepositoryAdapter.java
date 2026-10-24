package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.RegistrationTrendPoint;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.model.UserRegistrationSummary;
import com.forehapp.store.userModule.domain.model.UserSearchResult;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final IUserJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public UserRepositoryAdapter(IUserJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
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
        return jpaRepository.countUsersWithStoreProfile();
    }

    @Override
    public long countNewSince(LocalDateTime from) {
        return jpaRepository.countByCreationDateAfterWithStoreProfile(from);
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

    private static final DateTimeFormatter TREND_DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public List<RegistrationTrendPoint> findRegistrationTrend(LocalDateTime from) {
        return jpaRepository.findRegistrationTrend(from).stream()
                .map(v -> new RegistrationTrendPoint(v.getDate().format(TREND_DATE_FMT), v.getCount()))
                .toList();
    }

    @Override
    public List<UserSearchResult> search(List<String> terms, int limit) {
        // Every term must match at least one field, so "juan aragon" finds "Juan Pablo" + "Aragon".
        StringBuilder jpql = new StringBuilder(
                "SELECT u.id, u.name, u.lastname, u.email, sp.phone, u.creationDate, sp.id "
                        + "FROM User u LEFT JOIN StoreProfile sp ON sp.user = u WHERE 1 = 1");
        for (int i = 0; i < terms.size(); i++) {
            String p = ":t" + i;
            jpql.append(" AND (LOWER(u.email) LIKE ").append(p).append(" ESCAPE '!'")
                .append(" OR LOWER(COALESCE(u.name, '')) LIKE ").append(p).append(" ESCAPE '!'")
                .append(" OR LOWER(COALESCE(u.lastname, '')) LIKE ").append(p).append(" ESCAPE '!'")
                .append(" OR COALESCE(sp.phone, '') LIKE ").append(p).append(" ESCAPE '!')");
        }
        jpql.append(" ORDER BY u.creationDate DESC");

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        for (int i = 0; i < terms.size(); i++) {
            query.setParameter("t" + i, "%" + escapeLike(terms.get(i)) + "%");
        }
        return query.setMaxResults(limit).getResultList().stream()
                .map(r -> new UserSearchResult((Long) r[0], (String) r[1], (String) r[2], (String) r[3],
                        (String) r[4], (LocalDateTime) r[5], r[6] != null))
                .toList();
    }

    private static String escapeLike(String term) {
        return term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
