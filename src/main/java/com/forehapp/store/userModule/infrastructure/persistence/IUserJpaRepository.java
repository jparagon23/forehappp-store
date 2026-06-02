package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IUserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND EXISTS (SELECT sp FROM StoreProfile sp WHERE sp.user = u AND sp.active = true)")
    Optional<User> findByEmailWithActiveStoreProfile(@Param("email") String email);

    @Query("SELECT u.id as id, u.name as name, u.lastname as lastname, u.email as email, sp.phone as phone, u.creationDate as creationDate FROM User u INNER JOIN StoreProfile sp ON sp.user = u ORDER BY u.creationDate DESC")
    List<UserRegistrationView> findRecentRegistrations(Pageable pageable);

    @Query(value = "SELECT DATE(u.creation_date) AS date, COUNT(*) AS count FROM users u INNER JOIN store_profiles sp ON sp.user_id = u.user_id WHERE u.creation_date >= :from GROUP BY DATE(u.creation_date) ORDER BY date ASC", nativeQuery = true)
    List<RegistrationTrendView> findRegistrationTrend(@Param("from") LocalDateTime from);

    @Query("SELECT COUNT(u) FROM User u WHERE EXISTS (SELECT sp FROM StoreProfile sp WHERE sp.user = u) AND u.creationDate > :date")
    long countByCreationDateAfterWithStoreProfile(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(u) FROM User u WHERE EXISTS (SELECT sp FROM StoreProfile sp WHERE sp.user = u)")
    long countUsersWithStoreProfile();

    @Query(value = "SELECT COUNT(*) FROM store_profiles WHERE phone IS NOT NULL AND phone != ''", nativeQuery = true)
    long countUsersWithPhone();

    @Query(value = "SELECT COUNT(*) FROM store_profiles WHERE active = 1", nativeQuery = true)
    long countActiveUsers();
}
