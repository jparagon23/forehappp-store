package com.forehapp.store.userModule.infrastructure.persistence;

import java.time.LocalDate;

public interface RegistrationTrendView {
    LocalDate getDate();
    Long getCount();
}
