package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.UserSearchResponse;

public interface SearchUserUseCase {
    UserSearchResponse searchByEmail(String email);
}
