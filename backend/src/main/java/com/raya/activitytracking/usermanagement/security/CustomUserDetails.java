package com.raya.activitytracking.usermanagement.security;


import com.raya.activitytracking.usermanagement.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRole()
                .getPermissions()
                .stream()
                .map(permission ->
                        new SimpleGrantedAuthority(permission.getName())
                )
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
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

    /**
     * Get the user ID for the authenticated user.
     * This is used by other modules (Activity, Reporting) to scope data by user.
     *
     * @return the user's ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Get the full User entity.
     * Use with caution - prefer getUserId() for data scoping.
     *
     * @return the User entity
     */
    public User getUser() {
        return user;
    }
}
