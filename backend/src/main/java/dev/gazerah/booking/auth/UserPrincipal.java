package dev.gazerah.booking.auth;

import dev.gazerah.booking.user.Role;
import dev.gazerah.booking.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final Long tenantId;
    private final String email;
    private final String password;
    private final Role role;

    public UserPrincipal(User user) {
        this.userId = user.getId();
        this.tenantId = user.getTenantId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.role = user.getRole();
    }

    public UserPrincipal(Long userId, Long tenantId, String email, Role role) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.password = null;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
