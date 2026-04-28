package com.ambianceholidays.security;

import com.ambianceholidays.domain.user.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class SecurityPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final UserRole role;
    private final UUID agentId;

    public SecurityPrincipal(UUID userId, String email, UserRole role, UUID agentId) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.agentId = agentId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public boolean isAgent() {
        return role == UserRole.B2B_AGENT;
    }

    public boolean isAdmin() {
        return role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN_OPS || role == UserRole.FLEET_MANAGER;
    }

    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }
}
