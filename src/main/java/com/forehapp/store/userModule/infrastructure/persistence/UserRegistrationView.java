package com.forehapp.store.userModule.infrastructure.persistence;

import java.time.LocalDateTime;

public interface UserRegistrationView {
    Long getId();
    String getName();
    String getLastname();
    String getEmail();
    String getPhone();
    LocalDateTime getCreationDate();
}
