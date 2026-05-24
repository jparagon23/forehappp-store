package com.forehapp.store.userModule.application.dto;

import com.forehapp.store.userModule.domain.model.User;
import lombok.Getter;

@Getter
public class UserSearchResponse {
    private final Long id;
    private final String name;
    private final String lastname;
    private final String email;

    public UserSearchResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.lastname = user.getLastname();
        this.email = user.getEmail();
    }
}
