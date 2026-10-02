package com.forehapp.store.repurchaseModule.infrastructure.persistence;

import com.forehapp.store.repurchaseModule.domain.model.EmailUnsubscribe;
import com.forehapp.store.repurchaseModule.domain.ports.out.IEmailUnsubscribeDao;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Repository
public class EmailUnsubscribeRepositoryImpl implements IEmailUnsubscribeDao {

    private final IEmailUnsubscribeJpaRepository jpaRepository;

    public EmailUnsubscribeRepositoryImpl(IEmailUnsubscribeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Set<String> findUnsubscribed(Collection<String> emails) {
        if (emails.isEmpty()) return Set.of();
        return new HashSet<>(jpaRepository.findEmailsIn(emails));
    }

    @Override
    public boolean exists(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void save(String email) {
        jpaRepository.saveAndFlush(new EmailUnsubscribe(email));
    }

    @Override
    public void delete(String email) {
        jpaRepository.deleteByEmail(email);
    }
}
