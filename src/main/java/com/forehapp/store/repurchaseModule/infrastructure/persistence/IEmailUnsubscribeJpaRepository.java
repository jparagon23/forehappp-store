package com.forehapp.store.repurchaseModule.infrastructure.persistence;

import com.forehapp.store.repurchaseModule.domain.model.EmailUnsubscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface IEmailUnsubscribeJpaRepository extends JpaRepository<EmailUnsubscribe, Long> {

    @Query("SELECT e.email FROM EmailUnsubscribe e WHERE e.email IN :emails")
    List<String> findEmailsIn(@Param("emails") Collection<String> emails);

    boolean existsByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}
