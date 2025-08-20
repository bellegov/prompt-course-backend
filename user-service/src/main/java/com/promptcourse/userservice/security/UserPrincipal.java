package com.promptcourse.userservice.security;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@Getter
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isSubscribed;

    public UserPrincipal(Long id, String username, Collection<? extends GrantedAuthority> authorities, boolean isSubscribed) {
        this.id = id;
        this.username = username;
        this.authorities = authorities;
        this.isSubscribed = isSubscribed;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
