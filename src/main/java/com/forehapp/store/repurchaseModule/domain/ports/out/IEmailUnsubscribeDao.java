package com.forehapp.store.repurchaseModule.domain.ports.out;

import java.util.Collection;
import java.util.Set;

public interface IEmailUnsubscribeDao {
    Set<String> findUnsubscribed(Collection<String> emails);
    boolean exists(String email);
    void save(String email);
    void delete(String email);
}
