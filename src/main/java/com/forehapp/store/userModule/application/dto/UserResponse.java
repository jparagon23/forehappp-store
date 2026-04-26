package com.forehapp.store.userModule.application.dto;

import com.forehapp.store.userModule.domain.model.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String email;
    private final String name;
    private final String lastname;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.lastname = user.getLastname();
    }
}
