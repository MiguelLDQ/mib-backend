package com.mib.backend.security;

import com.mib.backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.enabled = user.isEnabled() && !user.isBanned();
        this.accountNonLocked = user.isAccountNonLocked() && !user.isCurrentlySuspended();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .toList();
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
