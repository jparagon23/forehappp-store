package com.forehapp.store.security.config;

import com.forehapp.store.userModule.domain.model.Role;
import com.forehapp.store.userModule.domain.model.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptyList();
        }
        return user.getRoles().stream()
                .filter(Objects::nonNull)
                .map(Role::getDescription)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(desc -> desc.startsWith("ROLE_") ? desc : "ROLE_" + desc.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return String.valueOf(user.getId());
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return user.getUserStatus() != null && user.getUserStatus() == 2;
    }
}
